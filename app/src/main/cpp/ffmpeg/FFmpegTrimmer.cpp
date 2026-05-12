#include "FFmpegTrimmer.h"
#include <jni.h>
#include <string>
#include <cstdint>
#include <vector>
#include <mutex>
#include <android/log.h>

#define LOG_TAG "FFmpegTrimmer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/error.h>
#include <libavutil/timestamp.h>
#include <libswresample/swresample.h>
}

// RAII wrapper for AVPacket
struct PacketGuard {
    AVPacket *pkt;
    PacketGuard() : pkt(av_packet_alloc()) {}
    ~PacketGuard() { av_packet_free(&pkt); }
    AVPacket* operator->() { return pkt; }
};

// RAII wrapper for AVFormatContext (input/output)
struct FormatContextGuard {
    AVFormatContext *ctx = nullptr;
    ~FormatContextGuard() {
        if (ctx) avformat_close_input(&ctx);
    }
};

// The main trimming function implementation
bool trim_video_ffmpeg(const char *inputPath, const char *outputPath,
                      int64_t startMs, int64_t endMs,
                      JNIEnv *env, jobject progressCallback) {
    avformat_network_init();

    FormatContextGuard inCtx;
    int ret = avformat_open_input(&inCtx.ctx, inputPath, nullptr, nullptr);
    if (ret < 0) {
        LOGE("Cannot open input: %s", av_err2str(ret));
        return false;
    }
    ret = avformat_find_stream_info(inCtx.ctx, nullptr);
    if (ret < 0) {
        LOGE("Cannot find stream info: %s", av_err2str(ret));
        return false;
    }

    // Find best video stream
    int videoStreamIdx = av_find_best_stream(inCtx.ctx, AVMEDIA_TYPE_VIDEO, -1, -1, nullptr, 0);
    if (videoStreamIdx < 0) {
        LOGE("No video stream found");
        return false;
    }
    AVStream *inVideoStream = inCtx.ctx->streams[videoStreamIdx];
    AVCodecParameters *codecpar = inVideoStream->codecpar;

    // Setup output
    AVFormatContext *outCtx = nullptr;
    avformat_alloc_output_context2(&outCtx, nullptr, "mp4", outputPath);
    if (!outCtx) {
        LOGE("Cannot allocate output context");
        return false;
    }
    
    // We'll manage outCtx manually for more control during the writing process
    // but we can use a simpler guard for the pointer itself.
    struct OutContextGuard {
        AVFormatContext *ctx;
        ~OutContextGuard() {
             if (ctx) {
                 if (!(ctx->oformat->flags & AVFMT_NOFILE))
                     avio_closep(&ctx->pb);
                 avformat_free_context(ctx);
             }
        }
    } outCtxGuard{outCtx};

    // Map input stream to output
    AVStream *outVideoStream = avformat_new_stream(outCtx, nullptr);
    avcodec_parameters_copy(outVideoStream->codecpar, codecpar);
    outVideoStream->time_base = inVideoStream->time_base;

    // Open output file
    if (!(outCtx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&outCtx->pb, outputPath, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Cannot open output file: %s", av_err2str(ret));
            return false;
        }
    }

    // Write header
    ret = avformat_write_header(outCtx, nullptr);
    if (ret < 0) {
        LOGE("Cannot write header: %s", av_err2str(ret));
        return false;
    }

    // Seek to start time (ms -> AVStream time_base)
    AVRational timeBase = inVideoStream->time_base;
    int64_t startPts = av_rescale_q(startMs, (AVRational){1, 1000}, timeBase);
    int64_t endPts = av_rescale_q(endMs, (AVRational){1, 1000}, timeBase);

    ret = av_seek_frame(inCtx.ctx, videoStreamIdx, startPts, AVSEEK_FLAG_BACKWARD);
    if (ret < 0) {
        LOGE("Seek failed: %s", av_err2str(ret));
        av_seek_frame(inCtx.ctx, videoStreamIdx, 0, AVSEEK_FLAG_BACKWARD);
    }

    // Progress tracking variables
    int64_t totalDuration = endPts - startPts;
    if (totalDuration <= 0) totalDuration = 1; // Avoid division by zero
    int64_t lastProgressSent = 0;
    const int64_t PROGRESS_STEP = 500; // send progress roughly every 500ms

    // Progress callback helper
    jclass progressClass = nullptr;
    jmethodID progressMethod = nullptr;
    if (env && progressCallback) {
        progressClass = env->GetObjectClass(progressCallback);
        // Kotlin lambda implements Function1.invoke(T)
        progressMethod = env->GetMethodID(progressClass, "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;");
    }

    PacketGuard packet;
    int64_t currentPts = 0;
    while (av_read_frame(inCtx.ctx, packet.pkt) >= 0) {
        if (packet->stream_index == videoStreamIdx) {
            AVPacket *pkt = packet.pkt;

            currentPts = pkt->pts;
            if (currentPts < startPts) {
                av_packet_unref(pkt);
                continue; 
            }
            if (currentPts > endPts) {
                av_packet_unref(pkt);
                break;
            }

            // Rescale timestamps
            pkt->pts = av_rescale_q(pkt->pts - startPts, timeBase, outVideoStream->time_base);
            pkt->dts = av_rescale_q(pkt->dts - startPts, timeBase, outVideoStream->time_base);
            pkt->duration = av_rescale_q(pkt->duration, timeBase, outVideoStream->time_base);
            pkt->pos = -1;
            pkt->stream_index = 0;

            ret = av_interleaved_write_frame(outCtx, pkt);
            if (ret < 0) {
                LOGE("Error writing frame: %s", av_err2str(ret));
                break;
            }

            // Progress callback
            int64_t progressPts = currentPts - startPts;
            if (progressPts - lastProgressSent > PROGRESS_STEP) {
                lastProgressSent = progressPts;
                if (progressCallback && progressMethod) {
                    float progress = (float) progressPts / (float) totalDuration;
                    
                    // Box float to Float
                    jclass floatCls = env->FindClass("java/lang/Float");
                    jmethodID floatInit = env->GetMethodID(floatCls, "<init>", "(F)V");
                    jobject floatObj = env->NewObject(floatCls, floatInit, progress);
                    
                    env->CallObjectMethod(progressCallback, progressMethod, floatObj);
                    
                    env->DeleteLocalRef(floatObj);
                    env->DeleteLocalRef(floatCls);
                }
            }
        }
        av_packet_unref(packet.pkt);
    }

    av_write_trailer(outCtx);
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_nativeTrimVideo(
    JNIEnv *env, jobject thiz,
    jstring inputPath,
    jstring outputPath,
    jlong startMs,
    jlong endMs,
    jobject progressCallback) {
    
    const char *nativeInputPath = env->GetStringUTFChars(inputPath, nullptr);
    const char *nativeOutputPath = env->GetStringUTFChars(outputPath, nullptr);

    bool result = trim_video_ffmpeg(nativeInputPath, nativeOutputPath, 
                                   (int64_t)startMs, (int64_t)endMs, 
                                   env, progressCallback);

    env->ReleaseStringUTFChars(inputPath, nativeInputPath);
    env->ReleaseStringUTFChars(outputPath, nativeOutputPath);

    return result ? JNI_TRUE : JNI_FALSE;
}
