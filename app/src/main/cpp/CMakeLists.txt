cmake_minimum_required(VERSION 3.18.1)
project("videoeditor_native")

# Set paths for prebuilt libraries (FFmpeg, libyuv etc would be placed here)
# For now, we link against standard Android libraries.
# Users will need to provide FFmpeg binaries separately.

add_library(native-lib SHARED
    native-lib.cpp
    # ffmpeg/FFmpegTrimmer.cpp
    gpu/Compositor.cpp
    gpu/TransformRenderer.cpp
    gpu/CanvasRenderer.cpp
    # ffmpeg/FFmpegFilter.cpp
    # ffmpeg/FFmpegEncoder.cpp
    # ffmpeg/FFmpegAudioMixer.cpp
    # gpu/GLRenderer.cpp
    # gpu/ShaderProgram.cpp
    # gpu/EffectsEngine.cpp
    # image/ThumbnailGenerator.cpp
    # audio/AudioProcessor.cpp
)

# Standard Android libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(gles3-lib GLESv3)
find_library(egl-lib EGL)

# FFmpeg libraries (assuming they are provided via mobile-ffmpeg or prebuilt)
# If using prebuilt .so files in jniLibs, you'd use add_library(avcodec SHARED IMPORTED) etc.
# For now, we'll try to find them or assume they are available if linked via gradle.

target_link_libraries(native-lib
    ${log-lib}
    ${android-lib}
    ${gles3-lib}
    ${egl-lib}
    # avcodec
    # avformat
    # avutil
    # swresample
    # swscale
)
