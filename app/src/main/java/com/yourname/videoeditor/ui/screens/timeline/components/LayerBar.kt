package com.yourname.videoeditor.ui.screens.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.videoeditor.domain.model.*

@Composable
fun LayerBar(
    layer: Layer,
    timeScale: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    onMove: (Long) -> Unit,
    onResizeStart: (Long) -> Unit,
    onResizeEnd: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (layer) {
        is VideoLayer -> Color(0xFF6200EE)
        is AudioLayer -> Color(0xFF03DAC5)
        is ImageLayer -> Color(0xFFFF0266)
        is TextLayer -> Color(0xFFFFDE03)
    }

    val contentColor = if (layer is TextLayer) Color.Black else Color.White
    val width = (layer.endMs - layer.startMs) * timeScale
    val offset = layer.startMs * timeScale

    Box(
        modifier = modifier
            .offset(x = offset.dp)
            .width(width.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor.copy(alpha = if (isSelected) 1.0f else 0.7f))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaMs = (dragAmount.x / timeScale).toLong()
                    onMove(layer.startMs + deltaMs)
                }
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        Text(
            text = layer.name,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Keyframe Markers Overlay
        val allKeyframes = (layer.opacity.keyframes + 
                           layer.animTransform.positionX.keyframes + 
                           layer.animTransform.positionY.keyframes + 
                           layer.animTransform.scaleX.keyframes + 
                           layer.animTransform.scaleY.keyframes + 
                           layer.animTransform.rotation.keyframes).distinctBy { it.timeMs }

        if (isSelected) {
            KeyframeMarkers(
                keyframes = allKeyframes.map { it.copy(timeMs = it.timeMs - layer.startMs) },
                timeScale = timeScale,
                onKeyframeMoved = { kf, newTimeRel ->
                    // Logic to move keyframe would go here
                },
                onKeyframeDoubleTap = { kf ->
                    // Logic to delete keyframe would go here
                }
            )
        }

    }
}
