package com.yourname.videoeditor.data.local

import android.content.Context

/**
 * ProjectFileManager handles saving and loading project states (JSON).
 */
class ProjectFileManager(private val context: Context) {
    fun saveProject(projectId: String, json: String) {
        // Save to internal storage
    }
    
    fun loadProject(projectId: String): String? {
        return null
    }
}
