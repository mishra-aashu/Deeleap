package com.yourname.videoeditor.ui.screens.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.yourname.videoeditor.domain.animation.Keyframe

@Composable
fun KeyframeMarkers(
    keyframes: List<Keyframe>,
    timeScale: Float, // Pixels per ms
    onKeyframeMoved: (Keyframe, Long) -> Unit,
    onKeyframeDoubleTap: (Keyframe) -> Unit
) {
    keyframes.forEach { kf ->
        val xOffset = (kf.timeMs * timeScale)
        
        Canvas(modifier = Modifier
            .offset(x = xOffset.dp)
            .size(12.dp)
            .pointerInput(kf) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newTimeMs = (kf.timeMs + (dragAmount.x / timeScale)).toLong()
                    onKeyframeMoved(kf, newTimeMs)
                }
            }
            .pointerInput(kf) {
                detectTapGestures(
                    onDoubleTap = { onKeyframeDoubleTap(kf) }
                )
            }
        ) {
            val path = Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(size.width / 2, size.height)
                lineTo(0f, size.height / 2)
                close()
            }
            drawPath(path, color = Color.Yellow)
        }
    }
}
