package com.yourname.videoeditor.domain.usecase

import android.net.Uri
import com.yourname.videoeditor.domain.model.ProcessingProgress
import com.yourname.videoeditor.processing.engine.LosslessTrimmer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class TrimVideoUseCase(
    private val losslessTrimmer: LosslessTrimmer
) {
    suspend fun execute(
        inputUri: Uri,
        outputFile: File,
        startMs: Long,
        endMs: Long
    ): Flow<ProcessingProgress> {
        return losslessTrimmer.trimVideo(
            inputUri = inputUri,
            outputFilePath = outputFile.absolutePath,
            startMs = startMs,
            endMs = endMs
        ).map { progress ->
            ProcessingProgress(progress, "Trimming video...")
        }
    }
}
