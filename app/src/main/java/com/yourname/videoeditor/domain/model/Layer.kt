package com.yourname.videoeditor.domain.model

import android.graphics.RectF
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.yourname.videoeditor.domain.animation.AnimatableProperty
import com.yourname.videoeditor.domain.animation.AnimatableTransform
import java.util.UUID

/**
 * Represents a transformation for a layer (position, scale, rotation).
 */
data class Transform(
    val positionX: Float = 0f, // normalized relative to canvas center (-1 to 1)
    val positionY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,  // degrees
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropRect: RectF? = null // in pixel space relative to original video
)


/**
 * Base sealed class for all timeline layers.
 */
sealed class Layer {
    abstract val id: String
    abstract val name: String
    abstract val startMs: Long
    abstract val endMs: Long
    abstract val sourceUri: Uri
    abstract val volume: Float
    abstract val isMuted: Boolean
    
    // Animation Properties
    abstract val opacity: AnimatableProperty
    abstract val animTransform: AnimatableTransform
    
    // Static Transform (Current state)
    abstract val transform: Transform

    val durationMs: Long get() = endMs - startMs
}

/**
 * A video track layer.
 */
data class VideoLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Video Clip",
    override val startMs: Long,
    override val endMs: Long,
    override val sourceUri: Uri,
    override val volume: Float = 1f,
    override val isMuted: Boolean = false,
    override val opacity: AnimatableProperty = AnimatableProperty(defaultValue = 1f),
    override val animTransform: AnimatableTransform = AnimatableTransform(),
    override val transform: Transform = Transform(),
    val trimInMs: Long = 0,
    val trimOutMs: Long = 0
) : Layer()

/**
 * An image/sticker layer.
 */
data class ImageLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Image",
    override val startMs: Long,
    override val endMs: Long,
    override val sourceUri: Uri,
    override val volume: Float = 1f,
    override val isMuted: Boolean = false,
    override val opacity: AnimatableProperty = AnimatableProperty(defaultValue = 1f),
    override val animTransform: AnimatableTransform = AnimatableTransform(),
    override val transform: Transform = Transform()
) : Layer()

/**
 * An audio track layer (background music, voiceover).
 */
data class AudioLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Audio",
    override val startMs: Long,
    override val endMs: Long,
    override val sourceUri: Uri,
    override val volume: Float = 1f,
    override val isMuted: Boolean = false,
    override val opacity: AnimatableProperty = AnimatableProperty(defaultValue = 0f), // Audio has no visual opacity
    override val animTransform: AnimatableTransform = AnimatableTransform(),
    override val transform: Transform = Transform()
) : Layer()

/**
 * A text layer for titles and captions.
 */
data class TextLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Text",
    override val startMs: Long,
    override val endMs: Long,
    override val sourceUri: Uri = Uri.EMPTY,
    override val volume: Float = 0f,
    override val isMuted: Boolean = false,
    override val opacity: AnimatableProperty = AnimatableProperty(defaultValue = 1f),
    override val animTransform: AnimatableTransform = AnimatableTransform(),
    override val transform: Transform = Transform(),
    val text: String = "Double tap to edit",
    val fontSize: Int = 24,
    val color: Color = Color.White,
    val fontPath: String? = null
) : Layer()


/**
 * Preset canvas ratios for different social media platforms.
 */
enum class CanvasRatio(val width: Int, val height: Int, val label: String) {
    REELS(1080, 1920, "Instagram Reels (9:16)"),
    YOUTUBE(1920, 1080, "YouTube (16:9)"),
    SQUARE(1080, 1080, "Square (1:1)")
}

/**
 * Represents a single track in the timeline, which can contain multiple non-overlapping layers.
 */
data class TimelineTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Track",
    val layers: List<Layer> = emptyList(),
    val isLocked: Boolean = false,
    val isVisible: Boolean = true
)

/**
 * Represents the entire editing project's timeline configuration.
 */
data class TimelineConfig(
    val tracks: List<TimelineTrack> = emptyList(),
    val ratio: CanvasRatio = CanvasRatio.REELS,
    val durationMs: Long = 0L
)


