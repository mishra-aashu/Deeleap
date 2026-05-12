#include "CanvasRenderer.h"
#include <android/log.h>
#include <vector>

#define LOG_TAG "CanvasRenderer"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

const char* CANVAS_VS = R"(#version 300 es
layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 aTexCoord;
out vec2 vTexCoord;
uniform mat4 uMatrix;

void main() {
    gl_Position = uMatrix * aPosition;
    vTexCoord = aTexCoord;
}
)";

const char* BLUR_FS = R"(#version 300 es
precision mediump float;
in vec2 vTexCoord;
out vec4 fragColor;
uniform sampler2D uTexture;
uniform vec2 uTexelSize;

void main() {
    vec4 color = vec4(0.0);
    float blurRadius = 4.0;
    for (float x = -blurRadius; x <= blurRadius; x += 2.0) {
        for (float y = -blurRadius; y <= blurRadius; y += 2.0) {
            color += texture(uTexture, vTexCoord + vec2(x, y) * uTexelSize);
        }
    }
    fragColor = color / 25.0; // Normalized for a 5x5-ish sample
}
)";

const char* COMPOSITE_FS = R"(#version 300 es
precision mediump float;
in vec2 vTexCoord;
out vec4 fragColor;
uniform sampler2D uBackgroundTex;
uniform sampler2D uForegroundTex;
uniform bool uIsForeground;

void main() {
    if (uIsForeground) {
        fragColor = texture(uForegroundTex, vTexCoord);
    } else {
        fragColor = texture(uBackgroundTex, vTexCoord);
    }
}
)";

CanvasRenderer::CanvasRenderer() : blurProgram(0), compositeProgram(0), vbo(0), 
                                   intermediateTex(0), intermediateFbo(0) {}

CanvasRenderer::~CanvasRenderer() {
    if (blurProgram) glDeleteProgram(blurProgram);
    if (compositeProgram) glDeleteProgram(compositeProgram);
    if (vbo) glDeleteBuffers(1, &vbo);
    if (intermediateTex) glDeleteTextures(1, &intermediateTex);
    if (intermediateFbo) glDeleteFramebuffers(1, &intermediateFbo);
}

void CanvasRenderer::init() {
    createPrograms();
    setupGeometry();
}

void CanvasRenderer::createPrograms() {
    auto loadShader = [](GLenum type, const char* source) {
        GLuint shader = glCreateShader(type);
        glShaderSource(shader, 1, &source, nullptr);
        glCompileShader(shader);
        return shader;
    };

    GLuint vs = loadShader(GL_VERTEX_SHADER, CANVAS_VS);
    GLuint blurFs = loadShader(GL_FRAGMENT_SHADER, BLUR_FS);
    GLuint compositeFs = loadShader(GL_FRAGMENT_SHADER, COMPOSITE_FS);

    blurProgram = glCreateProgram();
    glAttachShader(blurProgram, vs);
    glAttachShader(blurProgram, blurFs);
    glLinkProgram(blurProgram);

    compositeProgram = glCreateProgram();
    glAttachShader(compositeProgram, vs);
    glAttachShader(compositeProgram, compositeFs);
    glLinkProgram(compositeProgram);

    glDeleteShader(vs);
    glDeleteShader(blurFs);
    glDeleteShader(compositeFs);
}

void CanvasRenderer::setupGeometry() {
    float vertices[] = {
        -1.0f,  1.0f,  0.0f, 0.0f,
        -1.0f, -1.0f,  0.0f, 1.0f,
         1.0f,  1.0f,  1.0f, 0.0f,
         1.0f, -1.0f,  1.0f, 1.0f,
    };
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
}

void CanvasRenderer::render(GLuint inputTex, int inputWidth, int inputHeight, 
                            int canvasWidth, int canvasHeight, GLuint outputFbo) {
    
    // 1. Generate Blurred Background
    // (Simplified: Rendering background first, then foreground)
    
    glBindFramebuffer(GL_FRAMEBUFFER, outputFbo);
    glViewport(0, 0, canvasWidth, canvasHeight);
    glClear(GL_COLOR_BUFFER_BIT);

    // Draw Background (Blurred & Scaled to fill)
    glUseProgram(blurProgram);
    float canvasAspect = (float)canvasWidth / canvasHeight;
    float inputAspect = (float)inputWidth / inputHeight;
    
    float bgScaleX = 1.0f;
    float bgScaleY = 1.0f;
    
    if (inputAspect > canvasAspect) {
        bgScaleX = inputAspect / canvasAspect;
    } else {
        bgScaleY = canvasAspect / inputAspect;
    }

    float bgMatrix[16] = {
        bgScaleX, 0.0f, 0.0f, 0.0f,
        0.0f, bgScaleY, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };

    glUniformMatrix4fv(glGetUniformLocation(blurProgram, "uMatrix"), 1, GL_FALSE, bgMatrix);
    glUniform2f(glGetUniformLocation(blurProgram, "uTexelSize"), 1.0f / inputWidth, 1.0f / inputHeight);
    
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, inputTex);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    // 2. Draw Foreground (Maintaining aspect ratio)
    glUseProgram(compositeProgram);
    
    float fgScaleX = 1.0f;
    float fgScaleY = 1.0f;

    if (inputAspect > canvasAspect) {
        fgScaleY = canvasAspect / inputAspect;
    } else {
        fgScaleX = inputAspect / canvasAspect;
    }

    float fgMatrix[16] = {
        fgScaleX, 0.0f, 0.0f, 0.0f,
        0.0f, fgScaleY, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };

    glUniformMatrix4fv(glGetUniformLocation(compositeProgram, "uMatrix"), 1, GL_FALSE, fgMatrix);
    glUniform1i(glGetUniformLocation(compositeProgram, "uIsForeground"), 1);
    
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}
