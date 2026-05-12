package com.yourname.videoeditor.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.videoeditor.data.local.dao.ProjectDao
import com.yourname.videoeditor.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel(private val projectDao: ProjectDao) : ViewModel() {
    val projects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun createNewProject(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                name = name,
                thumbnailUri = null,
                timelineJson = "{}", // Initial empty timeline
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )
            val id = projectDao.insertProject(newProject)
            onCreated(id)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.deleteProject(project)
        }
    }
}
