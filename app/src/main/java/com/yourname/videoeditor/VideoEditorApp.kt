package com.yourname.videoeditor

import android.app.Application
import com.yourname.videoeditor.di.AppContainer

class VideoEditorApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
