package com.yourname.videoeditor.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourname.videoeditor.data.local.dao.ProjectDao

class HomeViewModelFactory(private val projectDao: ProjectDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(projectDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
