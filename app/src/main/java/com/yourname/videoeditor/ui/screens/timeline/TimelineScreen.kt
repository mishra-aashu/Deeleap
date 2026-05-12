package com.yourname.videoeditor.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.*
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
        initialUri?.let { uri ->
            viewModel.onVideoSelected(uri)
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    // Sync Player with Timeline Position
    LaunchedEffect(viewModel.currentPositionMs) {
        if (!exoPlayer.isPlaying) {
            exoPlayer.seekTo(viewModel.currentPositionMs)
        }
    }

    // Update Timeline Position while playing
    LaunchedEffect(exoPlayer.isPlaying) {
        while (exoPlayer.isPlaying) {
            viewModel.updatePosition(exoPlayer.currentPosition)
            delay(33) // ~30fps update
        }
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
            // 1. Preview Player (Larger Weight)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f) // Increased preview size
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            viewModel.selectedLayer?.let { (trackId, layer) ->
                                val currentTransform = layer.transform
                                val newPosX = currentTransform.positionX + (pan.x / size.width) * 2f
                                val newPosY = currentTransform.positionY + (pan.y / size.height) * 2f
                                val newScale = (currentTransform.scaleX * zoom).coerceIn(0.1f, 10f)
                                val newRotation = (currentTransform.rotation + rotation) % 360f

                                val updatedLayer = when (layer) {
                                    is VideoLayer -> layer.copy(transform = currentTransform.copy(positionX = newPosX, positionY = newPosY, scaleX = newScale, scaleY = newScale, rotation = newRotation))
                                    is ImageLayer -> layer.copy(transform = currentTransform.copy(positionX = newPosX, positionY = newPosY, scaleX = newScale, scaleY = newScale, rotation = newRotation))
                                    is TextLayer -> layer.copy(transform = currentTransform.copy(positionX = newPosX, positionY = newPosY, scaleX = newScale, scaleY = newScale, rotation = newRotation))
                                    else -> layer
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

                // Overlay Play/Pause
                IconButton(
                    onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        if (exoPlayer.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // 2. Timeline Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f) // Adjusted timeline area
            ) {
                MultiTrackTimeline(
                    config = viewModel.timelineConfig,
                    currentPositionMs = viewModel.currentPositionMs,
                    selectedLayerId = viewModel.selectedLayer?.second?.id,
                    onLayerSelected = { trackId, layer ->
                        viewModel.selectLayer(trackId, layer)
                        // Don't auto-show transform, just select
                    },
                    onLayerModified = { trackId, layer ->
                        viewModel.updateLayer(trackId, layer)
                    },
                    onAddTrack = { viewModel.addTrack() },
                    modifier = Modifier.weight(1f)
                )

                // 3. Bottom Tool Context Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .animateContentSize()
                ) {
                    if (viewModel.activeTool == "transform" && viewModel.selectedLayer != null) {
                        val (trackId, layer) = viewModel.selectedLayer!!
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
                                val vmProperty = if (property == "scale") "scaleX" else property
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
                                    else -> layer
                                }
                                viewModel.updateLayer(trackId, updatedLayer)
                            }
                        )
                    } else if (viewModel.selectedLayer != null) {
                        // Bottom Tool Bar (Horizontal Menu)
                        ToolSelectionBar(
                            activeTool = viewModel.activeTool,
                            onToolSelected = { viewModel.setActiveTool(if (viewModel.activeTool == it) null else it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolSelectionBar(
    activeTool: String?,
    onToolSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))
        ToolItem(name = "Trim", icon = Icons.Default.ContentCut, isSelected = activeTool == "trim") { onToolSelected("trim") }
        ToolItem(name = "Split", icon = Icons.AutoMirrored.Filled.CallSplit, isSelected = activeTool == "split") { onToolSelected("split") }
        ToolItem(name = "Transform", icon = Icons.Default.Transform, isSelected = activeTool == "transform") { onToolSelected("transform") }
        ToolItem(name = "Filters", icon = Icons.Default.Tune, isSelected = activeTool == "filters") { onToolSelected("filters") }
        ToolItem(name = "Volume", icon = Icons.Default.VolumeUp, isSelected = activeTool == "volume") { onToolSelected("volume") }
        ToolItem(name = "Delete", icon = Icons.Default.Delete, isSelected = activeTool == "delete") { onToolSelected("delete") }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
fun ToolItem(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


