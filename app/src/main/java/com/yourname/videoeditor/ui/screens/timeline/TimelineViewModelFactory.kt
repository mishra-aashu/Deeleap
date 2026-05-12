package com.yourname.videoeditor.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourname.videoeditor.data.local.dao.ProjectDao
import com.yourname.videoeditor.data.repository.VideoEditRepository

class TimelineViewModelFactory(
    private val videoEditRepository: VideoEditRepository,
    private val projectDao: ProjectDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimelineViewModel(videoEditRepository, projectDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
