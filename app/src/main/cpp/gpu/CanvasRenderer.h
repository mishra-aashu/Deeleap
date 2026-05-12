#ifndef VIDEO_EDITOR_CANVAS_RENDERER_H
#define VIDEO_EDITOR_CANVAS_RENDERER_H

#include <GLES3/gl3.h>

class CanvasRenderer {
public:
    CanvasRenderer();
    ~CanvasRenderer();

    void init();
    void render(GLuint inputTex, int inputWidth, int inputHeight, 
                int canvasWidth, int canvasHeight, GLuint outputFbo);

private:
    GLuint blurProgram;
    GLuint compositeProgram;
    GLuint vbo;
    GLuint intermediateTex;
    GLuint intermediateFbo;

    void createPrograms();
    void setupGeometry();
    void applyBlur(GLuint inputTex, int width, int height);
};

#endif //VIDEO_EDITOR_CANVAS_RENDERER_H
