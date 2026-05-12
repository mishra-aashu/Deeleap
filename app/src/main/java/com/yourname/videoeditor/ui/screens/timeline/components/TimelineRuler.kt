package com.yourname.videoeditor.ui.screens.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor

@Composable
fun TimelineRuler(
    totalDurationMs: Long,
    timeScale: Float, // Pixels per millisecond
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 10.sp
    )
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val stepMs = when {
            timeScale > 1.0f -> 100L // 0.1s
            timeScale > 0.1f -> 1000L // 1s
            timeScale > 0.01f -> 5000L // 5s
            else -> 10000L // 10s
        }

        val startMs = 0L
        val endMs = totalDurationMs

        for (timeMs in startMs..endMs step (stepMs / 10)) {
            val x = timeMs * timeScale
            val isMajor = timeMs % stepMs == 0L
            val lineHeight = if (isMajor) 15.dp.toPx() else 8.dp.toPx()
            
            drawLine(
                color = lineColor,
                start = Offset(x, size.height),
                end = Offset(x, size.height - lineHeight),
                strokeWidth = if (isMajor) 2f else 1f
            )

            if (isMajor) {
                val seconds = timeMs / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                val label = String.format("%02d:%02d", minutes, remainingSeconds)
                
                val textLayoutResult = textMeasurer.measure(label, labelStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(x + 4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}
