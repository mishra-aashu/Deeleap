package com.yourname.videoeditor.domain.model

import android.net.Uri

data class VideoClip(
    val id: String,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val startTrimMs: Long = 0,
    val endTrimMs: Long = durationMs
)

data class ProcessingProgress(
    val progress: Float,
    val status: String
)
