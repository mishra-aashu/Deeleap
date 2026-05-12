package com.yourname.videoeditor.processing.ffmpeg

/**
 * FFmpegCommandBuilder provides a Kotlin DSL to build FFmpeg commands easily.
 */
class FFmpegCommandBuilder {
    private val commandList = mutableListOf<String>()
    private var inputPath: String? = null
    private var outputPath: String? = null

    fun input(path: String) = apply {
        inputPath = path
    }

    fun output(path: String) = apply {
        outputPath = path
    }

    fun trim(startMs: Long, endMs: Long) = apply {
        val startSec = startMs / 1000.0
        val durationSec = (endMs - startMs) / 1000.0
        commandList.add("-ss")
        commandList.add(startSec.toString())
        commandList.add("-t")
        commandList.add(durationSec.toString())
    }

    fun videoCodec(codec: String) = apply {
        commandList.add("-c:v")
        commandList.add(codec)
    }

    fun audioCodec(codec: String) = apply {
        commandList.add("-c:a")
        commandList.add(codec)
    }

    fun custom(arg: String) = apply {
        commandList.add(arg)
    }

    fun build(): String {
        val finalArgs = mutableListOf("ffmpeg", "-y")
        
        // Add input before other args for better seeking performance if -ss is before -i
        // But here we'll follow a standard structure
        inputPath?.let {
            finalArgs.add("-i")
            finalArgs.add(it)
        }
        
        finalArgs.addAll(commandList)
        
        outputPath?.let {
            finalArgs.add(it)
        }
        
        return finalArgs.joinToString(" ")
    }
    
    fun buildArray(): Array<String> {
        val finalArgs = mutableListOf("-y")
        inputPath?.let {
            finalArgs.add("-i")
            finalArgs.add(it)
        }
        finalArgs.addAll(commandList)
        outputPath?.let {
            finalArgs.add(it)
        }
        return finalArgs.toTypedArray()
    }
}
