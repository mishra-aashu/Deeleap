#include <jni.h>
#include <string>
#include <android/log.h>
// #include "ffmpeg/FFmpegTrimmer.h"
#include "gpu/Compositor.h"
#include "gpu/TransformRenderer.h"
#include "gpu/CanvasRenderer.h"

#define LOG_TAG "NativeVideoEditor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global instances
Compositor* gCompositor = nullptr;
TransformRenderer* gTransformRenderer = nullptr;
CanvasRenderer* gCanvasRenderer = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_renderCanvasFrame(
        JNIEnv* env, jobject /* this */, 
        jint inputTextureId, jint inputWidth, jint inputHeight,
        jint canvasWidth, jint canvasHeight, jint outputFboId) {
    
    if (!gCanvasRenderer) {
        gCanvasRenderer = new CanvasRenderer();
        gCanvasRenderer->init();
    }

    gCanvasRenderer->render(inputTextureId, inputWidth, inputHeight, 
                           canvasWidth, canvasHeight, outputFboId);
    
    return JNI_TRUE;
}


extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_applyTransformFrame(
        JNIEnv* env, jobject /* this */, 
        jint inputTextureId,
        jfloat posX, jfloat posY, 
        jfloat scaleX, jfloat scaleY,
        jfloat rotation,
        jboolean flipH, jboolean flipV,
        jfloat cropL, jfloat cropT, jfloat cropR, jfloat cropB,
        jfloat opacity,
        jint outputTextureId, jint width, jint height) {
    
    if (!gTransformRenderer) {
        gTransformRenderer = new TransformRenderer();
        gTransformRenderer->init();
    }

    TransformParams params = {
        posX, posY, scaleX, scaleY, rotation, 
        (bool)flipH, (bool)flipV, 
        cropL, cropT, cropR, cropB,
        opacity
    };

    gTransformRenderer->render(inputTextureId, params, width, height);
    
    return JNI_TRUE;
}



extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_compositeFrame(
        JNIEnv* env, jobject /* this */, 
        jint bgTextureId, jint overlayTextureId,
        jfloat posX, jfloat posY, jfloat scale, jfloat rotation,
        jint outputTextureId, jint width, jint height) {
    
    if (!gCompositor) {
        gCompositor = new Compositor();
    }

    gCompositor->composite(bgTextureId, overlayTextureId, posX, posY, scale, rotation, 
                           outputTextureId, width, height);
    
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_applyGLEffect(
        JNIEnv* env,
        jobject /* this */,
        jint inputTextureId,
        jint outputTextureId,
        jint shaderType,
        jfloatArray parameterValues,
        jint width,
        jint height) {
    
    // TODO: Implement OpenGL effect processing
    return JNI_TRUE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_generateThumbnailsNative(
        JNIEnv* env,
        jobject /* this */,
        jstring videoPath,
        jlongArray timestampsMs,
        jint maxWidth,
        jint maxHeight) {
    
    // TODO: Implement FFmpeg-based fast thumbnail generation
    return nullptr;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_mixAudioTracks(
        JNIEnv* env,
        jobject /* this */,
        jstring inputPath1,
        jstring inputPath2,
        jstring outputPath,
        jfloatArray volumes1,
        jfloatArray volumes2) {
    
    // TODO: Implement native audio mixing
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourname_videoeditor_native_NativeBridge_extractAudioNative(
        JNIEnv* env,
        jobject /* this */,
        jstring inputPath,
        jstring outputPath) {
    
    // TODO: Implement FFmpeg-based audio extraction
    return JNI_TRUE;
}
