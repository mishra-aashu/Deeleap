package com.yourname.videoeditor.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.yourname.videoeditor.VideoEditorApp
import com.yourname.videoeditor.ui.screens.timeline.components.MultiTrackTimeline
import com.yourname.videoeditor.ui.screens.timeline.components.TransformControls
import androidx.compose.animation.animateContentSize
import com.yourname.videoeditor.domain.model.*




import com.yourname.videoeditor.ui.screens.timeline.components.CanvasRatioSelector

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    projectId: Long,
    onAddMedia: () -> Unit,
    initialUri: android.net.Uri? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as VideoEditorApp
    val viewModel: TimelineViewModel = viewModel(
        factory = TimelineViewModelFactory(
            app.container.videoEditRepository,
            app.container.database.projectDao()
        )
    )

    LaunchedEffect(projectId) {
        if (projectId != -1L) {
            viewModel.loadProject(projectId)
        }
    }

    LaunchedEffect(initialUri) {
        initialUri?.let { viewModel.onVideoSelected(it) }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Video Editor")
                        Spacer(Modifier.width(16.dp))
                        CanvasRatioSelector(
                            selectedRatio = viewModel.timelineConfig.ratio,
                            onRatioSelected = { viewModel.updateCanvasRatio(it) }
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.canUndo
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.canRedo
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = onAddMedia) {
                        Icon(Icons.Default.Add, contentDescription = "Add Media")
                    }
                    Button(
                        onClick = { viewModel.exportVideo(context.cacheDir) },
                        enabled = !viewModel.isProcessing,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Export")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Preview Player with Direct Manipulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            viewModel.selectedLayer?.let { (trackId, layer) ->
                                val currentTransform = layer.transform
                                
                                // Normalize pan based on viewport size (approximate)
                                val newPosX = currentTransform.positionX + (pan.x / size.width) * 2f
                                val newPosY = currentTransform.positionY + (pan.y / size.height) * 2f
                                val newScale = (currentTransform.scaleX * zoom).coerceIn(0.1f, 10f)
                                val newRotation = (currentTransform.rotation + rotation) % 360f

                                val updatedLayer = when (layer) {
                                    is VideoLayer -> layer.copy(
                                        transform = currentTransform.copy(
                                            positionX = newPosX,
                                            positionY = newPosY,
                                            scaleX = newScale,
                                            scaleY = newScale,
                                            rotation = newRotation
                                        )
                                    )
                                    is AudioLayer -> layer // Audio doesn't transform visually
                                    is ImageLayer -> layer.copy(
                                        transform = currentTransform.copy(
                                            positionX = newPosX,
                                            positionY = newPosY,
                                            scaleX = newScale,
                                            scaleY = newScale,
                                            rotation = newRotation
                                        )
                                    )
                                    is TextLayer -> layer.copy(
                                        transform = currentTransform.copy(
                                            positionX = newPosX,
                                            positionY = newPosY,
                                            scaleX = newScale,
                                            scaleY = newScale,
                                            rotation = newRotation
                                        )
                                    )
                                }
                                viewModel.updateLayer(trackId, updatedLayer)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                
                // Overlay controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { 
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                    ) {
                        Icon(
                            if (exoPlayer.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                }
            }

            // Processing Progress
            if (viewModel.isProcessing) {
                LinearProgressIndicator(
                    progress = viewModel.processingProgress?.progress ?: 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = viewModel.processingProgress?.status ?: "Processing...",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Timeline and Properties Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                // Properties Panel (Floating or Bottom depending on selection)
                viewModel.selectedLayer?.let { (trackId, layer) ->
                    TransformControls(
                        opacity = layer.opacity.getValueAt(viewModel.currentPositionMs),
                        posX = layer.transform.positionX,
                        posY = layer.transform.positionY,
                        scale = layer.transform.scaleX,
                        rotation = layer.transform.rotation,
                        onValueChange = { property, value ->
                            val currentTransform = layer.transform
                            val updatedLayer = when (layer) {
                                is VideoLayer -> {
                                    if (property == "opacity") layer.copy(opacity = layer.opacity.copy(defaultValue = value))
                                    else layer.copy(transform = when(property) {
                                        "posX" -> currentTransform.copy(positionX = value)
                                        "posY" -> currentTransform.copy(positionY = value)
                                        "scale" -> currentTransform.copy(scaleX = value, scaleY = value)
                                        "rotation" -> currentTransform.copy(rotation = value)
                                        else -> currentTransform
                                    })
                                }
                                is ImageLayer -> {
                                    if (property == "opacity") layer.copy(opacity = layer.opacity.copy(defaultValue = value))
                                    else layer.copy(transform = when(property) {
                                        "posX" -> currentTransform.copy(positionX = value)
                                        "posY" -> currentTransform.copy(positionY = value)
                                        "scale" -> currentTransform.copy(scaleX = value, scaleY = value)
                                        "rotation" -> currentTransform.copy(rotation = value)
                                        else -> currentTransform
                                    })
                                }
                                is TextLayer -> {
                                    if (property == "opacity") layer.copy(opacity = layer.opacity.copy(defaultValue = value))
                                    else layer.copy(transform = when(property) {
                                        "posX" -> currentTransform.copy(positionX = value)
                                        "posY" -> currentTransform.copy(positionY = value)
                                        "scale" -> currentTransform.copy(scaleX = value, scaleY = value)
                                        "rotation" -> currentTransform.copy(rotation = value)
                                        else -> currentTransform
                                    })
                                }
                                is AudioLayer -> layer
                            }
                            viewModel.updateLayer(trackId, updatedLayer)
                        },
                        onAddKeyframe = { property ->
                            // Map property names to match ViewModel's addKeyframe
                            val vmProperty = when(property) {
                                "scale" -> "scaleX" // Simplified
                                else -> property
                            }
                            // We need a current value to add as keyframe
                            val value = when(property) {
                                "opacity" -> layer.opacity.getValueAt(viewModel.currentPositionMs)
                                "posX" -> layer.transform.positionX
                                "posY" -> layer.transform.positionY
                                "scale" -> layer.transform.scaleX
                                "rotation" -> layer.transform.rotation
                                else -> 0f
                            }
                            viewModel.addKeyframe(trackId, layer.id, vmProperty, value)
                        },
                        onReset = {
                            val updatedLayer = when (layer) {
                                is VideoLayer -> layer.copy(transform = Transform())
                                is ImageLayer -> layer.copy(transform = Transform())
                                is TextLayer -> layer.copy(transform = Transform())
                                is AudioLayer -> layer
                            }
                            viewModel.updateLayer(trackId, updatedLayer)
                        },
                        modifier = Modifier.animateContentSize()
                    )
                }

                MultiTrackTimeline(
                    config = viewModel.timelineConfig,
                    currentPositionMs = viewModel.currentPositionMs,
                    selectedLayerId = viewModel.selectedLayer?.second?.id,
                    onLayerSelected = { trackId, layer ->
                        viewModel.selectLayer(trackId, layer)
                    },
                    onLayerModified = { trackId, layer ->
                        viewModel.updateLayer(trackId, layer)
                    },
                    onAddTrack = { viewModel.addTrack() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


