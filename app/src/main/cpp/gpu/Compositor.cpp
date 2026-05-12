#include "Compositor.h"
#include <android/log.h>
#include <GLES3/gl3.h>

#define LOG_TAG "GPUCompositor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

Compositor::Compositor() : mProgram(0), mFbo(0) {
    initShaders();
}

Compositor::~Compositor() {
    if (mFbo) glDeleteFramebuffers(1, &mFbo);
    if (mProgram) glDeleteProgram(mProgram);
}

void Compositor::initShaders() {
    const char* vertexShaderSource = R"(
        #version 300 es
        layout(location = 0) in vec4 aPosition;
        layout(location = 1) in vec2 aTexCoord;
        out vec2 vTexCoord;
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    )";

    const char* fragmentShaderSource = R"(
        #version 300 es
        precision mediump float;
        uniform sampler2D uTexture;
        in vec2 vTexCoord;
        out vec4 fragColor;
        void main() {
            fragColor = texture(uTexture, vTexCoord);
        }
    )";

    // Simple shader compilation logic (abbreviated for brevity)
    // In a real app, you'd use a helper to compile and link.
    mProgram = glCreateProgram();
    // (Shader compilation and linking would go here)
    LOGI("Compositor shaders initialized (placeholder logic)");
}

void Compositor::composite(int bgTextureId, int overlayTextureId, 
                          float posX, float posY, float scale, float rotation,
                          int outputTextureId, int width, int height) {
    
    if (mFbo == 0) glGenFramebuffers(1, &mFbo);
    
    glBindFramebuffer(GL_FRAMEBUFFER, mFbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, outputTextureId, 0);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Framebuffer incomplete");
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return;
    }

    glViewport(0, 0, width, height);
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(mProgram);

    // 1. Render Background
    // (Rendering logic using bgTextureId)

    // 2. Render Overlay with Transform
    // (Rendering logic using overlayTextureId and transform uniforms)

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}
