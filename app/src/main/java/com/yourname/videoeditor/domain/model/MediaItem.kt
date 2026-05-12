package com.yourname.videoeditor.domain.model

import android.net.Uri

sealed class MediaItem {
    abstract val id: Long
    abstract val uri: Uri
    abstract val displayName: String
    abstract val size: Long
    abstract val dateAdded: Long
}

data class ImageItem(
    override val id: Long,
    override val uri: Uri,
    override val displayName: String,
    override val size: Long,
    override val dateAdded: Long
) : MediaItem()

data class VideoItem(
    override val id: Long,
    override val uri: Uri,
    override val displayName: String,
    override val size: Long,
    override val dateAdded: Long,
    val durationMs: Long
) : MediaItem()

data class AudioItem(
    override val id: Long,
    override val uri: Uri,
    override val displayName: String,
    override val size: Long,
    override val dateAdded: Long,
    val durationMs: Long,
    val artist: String?,
    val album: String?
) : MediaItem()
