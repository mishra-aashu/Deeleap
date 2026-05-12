package com.yourname.videoeditor.data.repository

import android.net.Uri
import com.yourname.videoeditor.domain.model.ProcessingProgress
import com.yourname.videoeditor.domain.usecase.TrimVideoUseCase
import kotlinx.coroutines.flow.Flow
import java.io.File

class VideoEditRepository(
    private val trimVideoUseCase: TrimVideoUseCase
) {
    suspend fun trimVideo(
        inputUri: Uri,
        outputFile: File,
        startMs: Long,
        endMs: Long
    ): Flow<ProcessingProgress> {
        return trimVideoUseCase.execute(inputUri, outputFile, startMs, endMs)
    }
}
