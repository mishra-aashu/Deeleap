package com.yourname.videoeditor.ui.screens.gallery.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yourname.videoeditor.domain.model.ImageItem
import com.yourname.videoeditor.domain.model.MediaItem

@Composable
fun ImageGrid(
    images: List<MediaItem>,
    onItemSelected: (MediaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(2.dp)
    ) {
        items(images) { item ->
            AsyncImage(
                model = item.uri,
                contentDescription = item.displayName,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clickable { onItemSelected(item) },
                contentScale = ContentScale.Crop
            )
        }
    }
}
