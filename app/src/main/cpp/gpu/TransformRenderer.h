#ifndef VIDEO_EDITOR_TRANSFORM_RENDERER_H
#define VIDEO_EDITOR_TRANSFORM_RENDERER_H

#include <GLES3/gl3.h>
#include <vector>

struct TransformParams {
    float posX, posY;
    float scaleX, scaleY;
    float rotation; // degrees
    bool flipH, flipV;
    float cropLeft, cropTop, cropRight, cropBottom; // 0.0 to 1.0
    float opacity;
};


class TransformRenderer {
public:
    TransformRenderer();
    ~TransformRenderer();

    void init();
    void render(GLuint inputTex, TransformParams params, int width, int height);

private:
    GLuint program;
    GLuint vbo;
    
    void createProgram();
    void setupGeometry();
};

#endif //VIDEO_EDITOR_TRANSFORM_RENDERER_H
