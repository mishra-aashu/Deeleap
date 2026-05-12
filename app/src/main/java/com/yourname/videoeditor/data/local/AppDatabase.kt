package com.yourname.videoeditor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourname.videoeditor.data.local.dao.ProjectDao
import com.yourname.videoeditor.data.local.entity.ProjectEntity

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        const val DATABASE_NAME = "video_editor_db"
    }
}
