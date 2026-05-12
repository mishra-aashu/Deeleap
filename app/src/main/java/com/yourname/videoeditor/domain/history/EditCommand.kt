package com.yourname.videoeditor.domain.history

import com.yourname.videoeditor.domain.model.*

/**
 * Sealed class representing an undoable edit operation on the timeline.
 */
sealed class EditCommand {
    abstract fun execute(config: TimelineConfig): TimelineConfig
    abstract fun undo(config: TimelineConfig): TimelineConfig
}

/**
 * Command to update the duration or trim points of a layer.
 */
data class TrimCommand(
    val trackId: String,
    val layerId: String,
    val oldStart: Long,
    val oldEnd: Long,
    val newStart: Long,
    val newEnd: Long
) : EditCommand() {
    override fun execute(config: TimelineConfig): TimelineConfig {
        return updateLayer(config, newStart, newEnd)
    }

    override fun undo(config: TimelineConfig): TimelineConfig {
        return updateLayer(config, oldStart, oldEnd)
    }

    private fun updateLayer(config: TimelineConfig, start: Long, end: Long): TimelineConfig {
        return config.copy(
            tracks = config.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(layers = track.layers.map { layer ->
                        if (layer.id == layerId) {
                            when (layer) {
                                is VideoLayer -> layer.copy(startMs = start, endMs = end)
                                is AudioLayer -> layer.copy(startMs = start, endMs = end)
                                is ImageLayer -> layer.copy(startMs = start, endMs = end)
                                is TextLayer -> layer.copy(startMs = start, endMs = end)
                            }
                        } else layer
                    })
                } else track
            }
        )
    }
}

/**
 * Command to update the transformation of a layer.
 */
data class TransformCommand(
    val trackId: String,
    val layerId: String,
    val oldTransform: Transform,
    val newTransform: Transform
) : EditCommand() {
    override fun execute(config: TimelineConfig) = updateTransform(config, newTransform)
    override fun undo(config: TimelineConfig) = updateTransform(config, oldTransform)

    private fun updateTransform(config: TimelineConfig, transform: Transform): TimelineConfig {
        return config.copy(
            tracks = config.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(layers = track.layers.map { layer ->
                        if (layer.id == layerId) {
                            when (layer) {
                                is VideoLayer -> layer.copy(transform = transform)
                                is AudioLayer -> layer.copy(transform = transform)
                                is ImageLayer -> layer.copy(transform = transform)
                                is TextLayer -> layer.copy(transform = transform)
                            }
                        } else layer
                    })
                } else track
            }
        )
    }
}

/**
 * Command to add a new layer to a track.
 */
data class AddLayerCommand(
    val trackId: String,
    val layer: Layer
) : EditCommand() {
    override fun execute(config: TimelineConfig): TimelineConfig {
        return config.copy(
            tracks = config.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(layers = track.layers + layer)
                } else track
            }
        )
    }

    override fun undo(config: TimelineConfig): TimelineConfig {
        return config.copy(
            tracks = config.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(layers = track.layers.filter { it.id != layer.id })
                } else track
            }
        )
    }
}
