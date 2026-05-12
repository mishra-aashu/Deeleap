package com.yourname.videoeditor.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourname.videoeditor.data.datasource.MediaDataSource

class GalleryViewModelFactory(
    private val mediaDataSource: MediaDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(mediaDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
