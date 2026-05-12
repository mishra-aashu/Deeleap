#ifndef VIDEOEDITOR_FFMPEGTRIMMER_H
#define VIDEOEDITOR_FFMPEGTRIMMER_H

#include <jni.h>
#include <cstdint>

extern "C" {
// The JNI function exported to Kotlin
JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_nativeTrimVideo(
    JNIEnv *env, jobject thiz,
    jstring inputPath,
    jstring outputPath,
    jlong startMs,
    jlong endMs,
    jobject progressCallback);

// Internal helper for other C++ modules if needed
bool trim_video_ffmpeg(const char *inputPath, const char *outputPath,
                      int64_t startMs, int64_t endMs,
                      JNIEnv *env, jobject progressCallback);
}

#endif //VIDEOEDITOR_FFMPEGTRIMMER_H
