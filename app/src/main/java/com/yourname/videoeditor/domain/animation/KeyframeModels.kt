package com.yourname.videoeditor.domain.animation

import kotlin.math.pow

/**
 * Supported interpolation types for keyframe transitions.
 */
enum class Interpolation {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    HOLD            // sudden change at the next keyframe
}

/**
 * Represents a single keyframe point in time for a specific property.
 */
data class Keyframe(
    val timeMs: Long,
    val value: Float,
    val interpolation: Interpolation = Interpolation.LINEAR
)

/**
 * Utility to calculate interpolated values between keyframes.
 */
object Interpolator {
    fun interpolate(t: Float, type: Interpolation): Float {
        return when (type) {
            Interpolation.LINEAR -> t
            Interpolation.EASE_IN -> t * t
            Interpolation.EASE_OUT -> t * (2 - t)
            Interpolation.EASE_IN_OUT -> if (t < 0.5f) 2 * t * t else -1 + (4 - 2 * t) * t
            Interpolation.HOLD -> if (t >= 1f) 1f else 0f
        }
    }
}

/**
 * A property that can vary over time through keyframes.
 */
data class AnimatableProperty(
    val keyframes: List<Keyframe> = emptyList(),
    val defaultValue: Float = 0f
) {
    /**
     * Calculates the value of the property at a specific timestamp.
     */
    fun getValueAt(timeMs: Long): Float {
        if (keyframes.isEmpty()) return defaultValue
        
        // Find the surrounding keyframes
        val sorted = keyframes.sortedBy { it.timeMs }
        if (timeMs <= sorted.first().timeMs) return sorted.first().value
        if (timeMs >= sorted.last().timeMs) return sorted.last().value

        var lower = sorted.first()
        var upper = sorted.last()
        for (i in 0 until sorted.size - 1) {
            if (timeMs >= sorted[i].timeMs && timeMs < sorted[i + 1].timeMs) {
                lower = sorted[i]
                upper = sorted[i + 1]
                break
            }
        }

        val duration = (upper.timeMs - lower.timeMs).toFloat()
        if (duration == 0f) return upper.value
        
        val progress = (timeMs - lower.timeMs).toFloat() / duration
        val easedProgress = Interpolator.interpolate(progress, upper.interpolation)
        
        return lower.value + easedProgress * (upper.value - lower.value)
    }
}

/**
 * Container for all animatable transformation properties.
 */
data class AnimatableTransform(
    val positionX: AnimatableProperty = AnimatableProperty(defaultValue = 0f),
    val positionY: AnimatableProperty = AnimatableProperty(defaultValue = 0f),
    val scaleX: AnimatableProperty = AnimatableProperty(defaultValue = 1f),
    val scaleY: AnimatableProperty = AnimatableProperty(defaultValue = 1f),
    val rotation: AnimatableProperty = AnimatableProperty(defaultValue = 0f)
)
