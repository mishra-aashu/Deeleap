package com.yourname.videoeditor.processing.engine

sealed class VideoCommand {
    data class Trim(val startMs: Long, val endMs: Long) : VideoCommand()
    object Split : VideoCommand()
    data class ApplyFilter(val filterName: String) : VideoCommand()
    data class Export(val settings: String) : VideoCommand()
}
