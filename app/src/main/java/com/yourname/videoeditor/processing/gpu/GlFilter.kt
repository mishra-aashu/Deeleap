package com.yourname.videoeditor.processing.gpu

/**
 * GlFilter represents an OpenGL filter to be applied to a frame.
 */
abstract class GlFilter(val fragmentShader: String) {
    // Logic for applying filter
}

class BrightnessFilter : GlFilter("brightness.frag")
