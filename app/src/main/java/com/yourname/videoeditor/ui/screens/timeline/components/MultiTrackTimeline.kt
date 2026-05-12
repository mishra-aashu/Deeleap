package com.yourname.videoeditor.ui.screens.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.videoeditor.domain.model.*

@Composable
fun MultiTrackTimeline(
    config: TimelineConfig,
    currentPositionMs: Long,
    selectedLayerId: String?,
    onLayerSelected: (trackId: String, layer: Layer) -> Unit,
    onLayerModified: (trackId: String, layer: Layer) -> Unit,
    onAddTrack: () -> Unit,
    modifier: Modifier = Modifier
) {

    var timeScale by remember { mutableStateOf(0.1f) } // pixels per ms
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        timeScale = (timeScale * zoomChange).coerceIn(0.01f, 2.0f)
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // Timeline Header (Ruler)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .transformable(state = transformState)
        ) {
            TimelineRuler(
                totalDurationMs = config.durationMs.coerceAtLeast(60000L),
                timeScale = timeScale,
                modifier = Modifier.width(((config.durationMs.coerceAtLeast(60000L)) * timeScale).dp)
            )
        }

        // Tracks Area
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                config.tracks.forEach { track ->
                    TrackRow(
                        track = track,
                        timeScale = timeScale,
                        selectedLayerId = selectedLayerId,
                        horizontalScrollState = horizontalScrollState,
                        onLayerSelected = { onLayerSelected(track.id, it) },
                        onLayerModified = { onLayerModified(track.id, it) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                }

                
                // Add Track Button
                TextButton(
                    onClick = onAddTrack,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Track")
                }
            }
            
            // Playhead indicator
            Playhead(
                positionMs = currentPositionMs,
                timeScale = timeScale,
                scrollOffset = horizontalScrollState.value
            )
        }
    }
}

@Composable
fun TrackRow(
    track: TimelineTrack,
    timeScale: Float,
    selectedLayerId: String?,
    horizontalScrollState: ScrollState,
    onLayerSelected: (Layer) -> Unit,
    onLayerModified: (Layer) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Track Header (Controls)
        Column(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(track.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (track.isVisible) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (track.isLocked) MaterialTheme.colorScheme.error else Color.Gray
                )
            }
        }

        // Track Content (Layers)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizontalScrollState)
        ) {
            track.layers.forEach { layer ->
                LayerBar(
                    layer = layer,
                    timeScale = timeScale,
                    isSelected = layer.id == selectedLayerId,
                    onClick = { onLayerSelected(layer) },
                    onMove = { newStart ->
                        val duration = layer.endMs - layer.startMs
                        onLayerModified(
                            when (layer) {
                                is VideoLayer -> layer.copy(startMs = newStart, endMs = newStart + duration)
                                is AudioLayer -> layer.copy(startMs = newStart, endMs = newStart + duration)
                                is ImageLayer -> layer.copy(startMs = newStart, endMs = newStart + duration)
                                is TextLayer -> layer.copy(startMs = newStart, endMs = newStart + duration)
                            }
                        )
                    },
                    onResizeStart = { /* Implement resize */ },
                    onResizeEnd = { /* Implement resize */ }
                )
            }
        }
    }
}


@Composable
fun Playhead(
    positionMs: Long,
    timeScale: Float,
    scrollOffset: Int
) {
    val x = (positionMs * timeScale) - scrollOffset
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)
            .offset(x = x.dp)
            .background(MaterialTheme.colorScheme.error)
    ) {
        // Playhead triangle at the top
        Box(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .background(MaterialTheme.colorScheme.error)
        )
    }
}
