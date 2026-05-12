package com.yourname.videoeditor.ui.screens.timeline

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.videoeditor.data.repository.VideoEditRepository
import com.yourname.videoeditor.data.local.dao.ProjectDao
import com.yourname.videoeditor.data.local.entity.ProjectEntity
import com.yourname.videoeditor.domain.model.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

import com.yourname.videoeditor.domain.history.*
import com.yourname.videoeditor.domain.animation.Keyframe

class TimelineViewModel(
    private val videoEditRepository: VideoEditRepository,
    private val projectDao: ProjectDao
) : ViewModel() {

    var currentProject by mutableStateOf<ProjectEntity?>(null)
        private set

    var timelineConfig by mutableStateOf(TimelineConfig())
        private set

    var currentPositionMs by mutableStateOf(0L)

    fun addKeyframe(trackId: String, layerId: String, propertyName: String, value: Float) {
        val layer = timelineConfig.tracks.find { it.id == trackId }?.layers?.find { it.id == layerId } ?: return
        
        val newKeyframe = Keyframe(currentPositionMs, value)
        
        val updatedLayer = when (layer) {
            is VideoLayer -> {
                when (propertyName) {
                    "opacity" -> layer.copy(opacity = layer.opacity.copy(keyframes = (layer.opacity.keyframes + newKeyframe).sortedBy { k -> k.timeMs }))
                    "posX" -> layer.copy(animTransform = layer.animTransform.copy(positionX = layer.animTransform.positionX.copy(keyframes = (layer.animTransform.positionX.keyframes + newKeyframe).sortedBy { k -> k.timeMs })))
                    "posY" -> layer.copy(animTransform = layer.animTransform.copy(positionY = layer.animTransform.positionY.copy(keyframes = (layer.animTransform.positionY.keyframes + newKeyframe).sortedBy { k -> k.timeMs })))
                    "scaleX" -> layer.copy(animTransform = layer.animTransform.copy(scaleX = layer.animTransform.scaleX.copy(keyframes = (layer.animTransform.scaleX.keyframes + newKeyframe).sortedBy { k -> k.timeMs })))
                    "scaleY" -> layer.copy(animTransform = layer.animTransform.copy(scaleY = layer.animTransform.scaleY.copy(keyframes = (layer.animTransform.scaleY.keyframes + newKeyframe).sortedBy { k -> k.timeMs })))
                    "rotation" -> layer.copy(animTransform = layer.animTransform.copy(rotation = layer.animTransform.rotation.copy(keyframes = (layer.animTransform.rotation.keyframes + newKeyframe).sortedBy { k -> k.timeMs })))
                    else -> layer
                }
            }
            else -> layer // Handle other layer types similarly
        }
        
        updateLayer(trackId, updatedLayer)
    }

    private val undoStack = mutableListOf<EditCommand>()
    private val redoStack = mutableListOf<EditCommand>()

    val canUndo get() = undoStack.isNotEmpty()
    val canRedo get() = redoStack.isNotEmpty()

    fun executeCommand(command: EditCommand) {
        timelineConfig = command.execute(timelineConfig)
        undoStack.add(command)
        redoStack.clear()
        
        // Update selection if the command affected the selected layer
        updateSelectionAfterCommand(command)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val command = undoStack.removeLast()
        timelineConfig = command.undo(timelineConfig)
        redoStack.add(command)
        
        updateSelectionAfterCommand(command)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val command = redoStack.removeLast()
        timelineConfig = command.execute(timelineConfig)
        undoStack.add(command)
        
        updateSelectionAfterCommand(command)
    }

    private fun updateSelectionAfterCommand(command: EditCommand) {
        // Simple logic to keep selection in sync
        val layerId = when(command) {
            is TrimCommand -> command.layerId
            is TransformCommand -> command.layerId
            is AddLayerCommand -> command.layer.id
            else -> null
        }
        
        // Find trackId for the layer
        val trackId = timelineConfig.tracks.find { track -> 
            track.layers.any { it.id == layerId } 
        }?.id
        
        val layer = timelineConfig.tracks.flatMap { it.layers }.find { it.id == layerId }
        if (trackId != null && layer != null) {
            selectedLayer = Pair(trackId, layer)
        }
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            val project = projectDao.getProjectById(projectId)
            currentProject = project
            // In a real app, we would parse timelineJson here
            // Initializing with some dummy tracks for now
            if (timelineConfig.tracks.isEmpty()) {
                timelineConfig = TimelineConfig(
                    tracks = listOf(
                        TimelineTrack(name = "Video 1"),
                        TimelineTrack(name = "Audio 1")
                    ),
                    durationMs = 30000L
                )
            }
        }
    }

    var selectedVideoUri by mutableStateOf<Uri?>(null)
        private set

    var processingProgress by mutableStateOf<ProcessingProgress?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var outputVideoFile by mutableStateOf<File?>(null)
        private set

    var activeTool by mutableStateOf<String?>(null)
        private set

    fun setActiveTool(tool: String?) {
        activeTool = tool
    }

    fun updatePosition(positionMs: Long) {
        currentPositionMs = positionMs
    }

    fun onVideoSelected(uri: Uri) {
        selectedVideoUri = uri
        val trackId = timelineConfig.tracks.firstOrNull()?.id ?: return
        val videoLayer = VideoLayer(
            startMs = 0,
            endMs = 5000, 
            sourceUri = uri
        )
        executeCommand(AddLayerCommand(trackId, videoLayer))
    }

    fun addTrack() {
        val newTrack = TimelineTrack(name = "New Track ${timelineConfig.tracks.size + 1}")
        timelineConfig = timelineConfig.copy(
            tracks = timelineConfig.tracks + newTrack
        )
    }

    var selectedLayer by mutableStateOf<Pair<String, Layer>?>(null)
        private set

    fun selectLayer(trackId: String, layer: Layer?) {
        selectedLayer = if (layer != null) Pair(trackId, layer) else null
    }

    fun updateLayer(trackId: String, updatedLayer: Layer) {

        val oldLayer = timelineConfig.tracks.find { it.id == trackId }?.layers?.find { it.id == updatedLayer.id } ?: return
        
        if (oldLayer.startMs != updatedLayer.startMs || oldLayer.endMs != updatedLayer.endMs) {
            executeCommand(TrimCommand(trackId, updatedLayer.id, oldLayer.startMs, oldLayer.endMs, updatedLayer.startMs, updatedLayer.endMs))
        } else if (oldLayer.transform != updatedLayer.transform) {
            executeCommand(TransformCommand(trackId, updatedLayer.id, oldLayer.transform, updatedLayer.transform))
        } else {
            // Update for other property changes
            val tracks = timelineConfig.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(layers = track.layers.map { if (it.id == updatedLayer.id) updatedLayer else it })
                } else track
            }
            timelineConfig = timelineConfig.copy(tracks = tracks)
        }
    }

    fun updateCanvasRatio(ratio: CanvasRatio) {
        timelineConfig = timelineConfig.copy(ratio = ratio)
    }

    fun exportVideo(cacheDir: File) {

        // Simplified export of the first layer for now
        val firstLayer = timelineConfig.tracks.flatMap { it.layers }.firstOrNull { it is VideoLayer } as? VideoLayer ?: return
        
        viewModelScope.launch {
            isProcessing = true
            val outputFile = File(cacheDir, "exported_video_${System.currentTimeMillis()}.mp4")
            
            videoEditRepository.trimVideo(
                firstLayer.sourceUri, 
                outputFile, 
                firstLayer.startMs, 
                firstLayer.endMs
            ).collectLatest { progress ->
                processingProgress = progress
            }
            
            outputVideoFile = outputFile
            isProcessing = false
        }
    }
}

