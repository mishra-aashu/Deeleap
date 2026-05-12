package com.yourname.videoeditor.processing.audio

import com.yourname.videoeditor.processing.ffmpeg.FFmpegCommandBuilder
import com.yourname.videoeditor.processing.ffmpeg.FFmpegExecutor

/**
 * AudioMixer handles volume control, merging audio tracks, and fades using FFmpeg.
 */
class AudioMixer(private val ffmpegExecutor: FFmpegExecutor) {

    /**
     * Merges a background music track with a video, adjusting volumes and adding fades.
     */
    suspend fun mergeAudio(
        videoPath: String,
        audioPath: String,
        outputPath: String,
        videoVolume: Float = 1.0f,
        audioVolume: Float = 0.5f,
        fadeInMs: Long = 1000,
        fadeOutMs: Long = 1000
    ): Int {
        val fadeInSec = fadeInMs / 1000.0
        val fadeOutSec = fadeOutMs / 1000.0
        
        // Build complex filter for audio mixing
        // [0:a] is video audio, [1:a] is background music
        val filter = "[0:a]volume=$videoVolume[a1]; " +
                    "[1:a]volume=$audioVolume, afade=t=in:st=0:d=$fadeInSec[a2]; " +
                    "[a1][a2]amix=inputs=2:duration=first[aout]"

        val command = "ffmpeg -y -i $videoPath -i $audioPath " +
                "-filter_complex \"$filter\" " +
                "-map 0:v -map \"[aout]\" -c:v copy -c:a aac $outputPath"

        return ffmpegExecutor.executeCommand(command)
    }
}
