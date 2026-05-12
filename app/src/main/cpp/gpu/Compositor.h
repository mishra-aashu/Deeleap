#ifndef VIDEOEDITOR_COMPOSITOR_H
#define VIDEOEDITOR_COMPOSITOR_H

#include <GLES3/gl3.h>

/**
 * Handles GPU-based frame composition for multi-track layering.
 */
class Compositor {
public:
    Compositor();
    ~Compositor();

    /**
     * Composites an overlay texture onto a background texture with transformations.
     */
    void composite(int bgTextureId, int overlayTextureId, 
                   float posX, float posY, float scale, float rotation,
                   int outputTextureId, int width, int height);

private:
    GLuint mProgram;
    GLuint mFbo;
    
    // Shader related methods would go here
    void initShaders();
};

#endif //VIDEOEDITOR_COMPOSITOR_H
