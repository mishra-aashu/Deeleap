package com.yourname.videoeditor.ui.screens.timeline.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourname.videoeditor.domain.model.CanvasRatio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasRatioSelector(
    selectedRatio: CanvasRatio,
    onRatioSelected: (CanvasRatio) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        InputChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(selectedRatio.label) },
            leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CanvasRatio.values().forEach { ratio ->
                DropdownMenuItem(
                    text = { Text(ratio.label) },
                    onClick = {
                        onRatioSelected(ratio)
                        expanded = false
                    },
                    trailingIcon = {
                        if (ratio == selectedRatio) {
                            RadioButton(selected = true, onClick = null)
                        }
                    }
                )
            }
        }
    }
}
