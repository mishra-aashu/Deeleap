package com.yourname.videoeditor.ui.screens.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.videoeditor.domain.animation.AnimatableTransform

@Composable
fun TransformControls(
    opacity: Float,
    posX: Float,
    posY: Float,
    scale: Float,
    rotation: Float,
    onValueChange: (String, Float) -> Unit,
    onAddKeyframe: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Transform & Animation", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Opacity
            PropertySlider(
                label = "Opacity",
                value = opacity,
                range = 0f..1f,
                onValueChange = { onValueChange("opacity", it) },
                onAddKeyframe = { onAddKeyframe("opacity") }
            )

            // Rotation
            PropertySlider(
                label = "Rotation",
                value = rotation,
                range = -180f..180f,
                onValueChange = { onValueChange("rotation", it) },
                onAddKeyframe = { onAddKeyframe("rotation") }
            )

            // Scale
            PropertySlider(
                label = "Scale",
                value = scale,
                range = 0.1f..5.0f,
                onValueChange = { onValueChange("scale", it) },
                onAddKeyframe = { onAddKeyframe("scale") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }
        }
    }
}

@Composable
fun PropertySlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onAddKeyframe: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${label}: ${"%.2f".format(value)}", fontSize = 12.sp)
            IconButton(onClick = onAddKeyframe, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Diamond, 
                    contentDescription = "Add Keyframe",
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

