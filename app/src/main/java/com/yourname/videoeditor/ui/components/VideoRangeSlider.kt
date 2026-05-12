package com.yourname.videoeditor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VideoRangeSlider(
    range: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier
) {
    var lowerValue by remember { mutableStateOf(range.start) }
    var upperValue by remember { mutableStateOf(range.endInclusive) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        val width = constraints.maxWidth.toFloat()
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val left = lowerValue * width
            val right = upperValue * width
            
            // Background overlay for trimmed parts
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = Size(left, size.height)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(right, 0f),
                size = Size(width - right, size.height)
            )
            
            // Selection border
            drawRect(
                color = Color.Yellow,
                topLeft = Offset(left, 0f),
                size = Size(right - left, size.height),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
        }

        // Handles
        Handle(
            position = lowerValue,
            onDrag = { delta ->
                lowerValue = (lowerValue + delta / width).coerceIn(0f, upperValue - 0.05f)
                onRangeChange(lowerValue..upperValue)
            },
            alignment = Alignment.CenterStart,
            width = width
        )

        Handle(
            position = upperValue,
            onDrag = { delta ->
                upperValue = (upperValue + delta / width).coerceIn(lowerValue + 0.05f, 1f)
                onRangeChange(lowerValue..upperValue)
            },
            alignment = Alignment.CenterEnd,
            width = width
        )
    }
}

@Composable
private fun Handle(
    position: Float,
    onDrag: (Float) -> Unit,
    alignment: Alignment,
    width: Float
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(20.dp)
            .offset(x = (position * width).dp / 2.7f) // Rough conversion for demo
            .background(Color.Yellow)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x)
                }
            }
    )
}
