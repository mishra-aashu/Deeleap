package com.yourname.videoeditor.di

import android.content.Context
import androidx.room.Room
import com.yourname.videoeditor.data.local.AppDatabase
import com.yourname.videoeditor.data.local.ThemePreferences
import com.yourname.videoeditor.data.repository.VideoEditRepository
import com.yourname.videoeditor.domain.usecase.TrimVideoUseCase
import com.yourname.videoeditor.processing.engine.LosslessTrimmer

/**
 * AppContainer is a manual Dependency Injection container.
 */
class AppContainer(private val context: Context) {
    
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    val themePreferences by lazy { ThemePreferences(context) }
    
    private val losslessTrimmer by lazy { LosslessTrimmer(context) }
    
    private val trimVideoUseCase by lazy { TrimVideoUseCase(losslessTrimmer) }
    
    val videoEditRepository by lazy { VideoEditRepository(trimVideoUseCase) }
    
    val mediaDataSource by lazy { 
        com.yourname.videoeditor.data.datasource.MediaDataSource(context.contentResolver) 
    }
}
