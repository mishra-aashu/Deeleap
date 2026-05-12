#include "TransformRenderer.h"
#include <android/log.h>
#include <cmath>
#include <GLES3/gl3.h>

#define LOG_TAG "TransformRenderer"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

const char* VERTEX_SHADER = R"(#version 300 es
layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 aTexCoord;
out vec2 vTexCoord;
uniform mat4 uMatrix;

void main() {
    gl_Position = uMatrix * aPosition;
    vTexCoord = aTexCoord;
}
)";

const char* FRAGMENT_SHADER = R"(#version 300 es
precision mediump float;
in vec2 vTexCoord;
out vec4 fragColor;
uniform sampler2D uTexture;
uniform vec4 uCropRect; // x, y, width, height (0-1 range)
uniform float uOpacity;

void main() {
    vec2 croppedCoord = vTexCoord * uCropRect.zw + uCropRect.xy;
    if (croppedCoord.x < 0.0 || croppedCoord.x > 1.0 || 
        croppedCoord.y < 0.0 || croppedCoord.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 0.0);
    } else {
        vec4 color = texture(uTexture, croppedCoord);
        fragColor = vec4(color.rgb, color.a * uOpacity);
    }
}
)";

TransformRenderer::TransformRenderer() : program(0), vbo(0) {}

TransformRenderer::~TransformRenderer() {
    if (program) glDeleteProgram(program);
    if (vbo) glDeleteBuffers(1, &vbo);
}

void TransformRenderer::init() {
    createProgram();
    setupGeometry();
}


void TransformRenderer::createProgram() {
    auto loadShader = [](GLenum type, const char* source) {
        GLuint shader = glCreateShader(type);
        glShaderSource(shader, 1, &source, nullptr);
        glCompileShader(shader);
        return shader;
    };

    GLuint vs = loadShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    GLuint fs = loadShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

    program = glCreateProgram();
    glAttachShader(program, vs);
    glAttachShader(program, fs);
    glLinkProgram(program);

    glDeleteShader(vs);
    glDeleteShader(fs);
}

void TransformRenderer::setupGeometry() {
    float vertices[] = {
        // Pos X, Y    Tex U, V
        -1.0f,  1.0f,  0.0f, 0.0f,
        -1.0f, -1.0f,  0.0f, 1.0f,
         1.0f,  1.0f,  1.0f, 0.0f,
         1.0f, -1.0f,  1.0f, 1.0f,
    };
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
}

void TransformRenderer::render(GLuint inputTex, TransformParams params, int width, int height) {
    glUseProgram(program);
    glViewport(0, 0, width, height);

    glUniform1f(glGetUniformLocation(program, "uOpacity"), params.opacity);

    // Calculate Matrix

    float rad = params.rotation * M_PI / 180.0f;
    float cosA = cos(rad);
    float sinA = sin(rad);

    float sx = params.scaleX * (params.flipH ? -1.0f : 1.0f);
    float sy = params.scaleY * (params.flipV ? -1.0f : 1.0f);

    // Identity Matrix with manual transforms applied
    float matrix[16] = {
        cosA * sx, -sinA * sy, 0.0f, 0.0f,
        sinA * sx,  cosA * sy, 0.0f, 0.0f,
        0.0f,       0.0f,      1.0f, 0.0f,
        params.posX, params.posY, 0.0f, 1.0f
    };

    GLuint matrixLoc = glGetUniformLocation(program, "uMatrix");
    glUniformMatrix4fv(matrixLoc, 1, GL_FALSE, matrix);

    GLuint cropLoc = glGetUniformLocation(program, "uCropRect");
    glUniform4f(cropLoc, params.cropLeft, params.cropTop, 
                params.cropRight - params.cropLeft, 
                params.cropBottom - params.cropTop);

    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, inputTex);
    glUniform1i(glGetUniformLocation(program, "uTexture"), 0);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}
