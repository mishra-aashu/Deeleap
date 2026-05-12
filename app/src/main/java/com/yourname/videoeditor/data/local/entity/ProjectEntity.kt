package com.yourname.videoeditor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val thumbnailUri: String?,
    val timelineJson: String,
    val createdAt: Long,
    val modifiedAt: Long
)
