package com.yourname.videoeditor.processing.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

/**
 * LosslessTrimmer uses MediaExtractor and MediaMuxer to trim video without re-encoding
 * for keyframe-aligned cuts. This is much faster than full re-encoding.
 */
class LosslessTrimmer(private val context: Context) {

    companion object {
        private const val TAG = "LosslessTrimmer"
        private const val DEFAULT_BUFFER_SIZE = 1024 * 1024 // 1MB
    }

    suspend fun trimVideo(
        inputUri: Uri,
        outputFilePath: String,
        startMs: Long,
        endMs: Long
    ): Flow<Float> = flow {
        withContext(Dispatchers.Default) {
            val startUs = startMs * 1000
            val endUs = endMs * 1000

            val extractor = MediaExtractor()
            var muxer: MediaMuxer? = null

            try {
                context.contentResolver.openFileDescriptor(inputUri, "r")?.use { fd ->
                    extractor.setDataSource(fd.fileDescriptor)
                } ?: throw Exception("Could not open input file")

                muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

                val trackCount = extractor.trackCount
                val trackIndices = IntArray(trackCount) { -1 }
                
                // Select tracks and add to muxer
                for (i in 0 until trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                    
                    if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                        extractor.selectTrack(i)
                        trackIndices[i] = muxer.addTrack(format)
                        Log.d(TAG, "Added track $i ($mime) to muxer")
                    }
                }

                muxer.start()

                val buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
                val bufferInfo = MediaCodec.BufferInfo()
                
                // Seek to start position
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

                var framesCopied = 0
                val totalDurationUs = (endUs - startUs).coerceAtLeast(1L)

                while (true) {
                    val trackIndex = extractor.sampleTrackIndex
                    if (trackIndex < 0) break

                    val presentationTimeUs = extractor.sampleTime
                    if (presentationTimeUs > endUs) break

                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    bufferInfo.presentationTimeUs = presentationTimeUs
                    bufferInfo.flags = extractor.sampleFlags

                    if (bufferInfo.size < 0) break

                    val muxerTrackIndex = trackIndices[trackIndex]
                    if (muxerTrackIndex >= 0) {
                        muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                        framesCopied++
                        
                        // Emit progress
                        if (framesCopied % 30 == 0) {
                            val progress = ((presentationTimeUs - startUs).toFloat() / totalDurationUs).coerceIn(0f, 1f)
                            emit(progress)
                        }
                    }
                    extractor.advance()
                }

                emit(1.0f)
                Log.d(TAG, "Trim finished. Copied $framesCopied frames.")

            } catch (e: Exception) {
                Log.e(TAG, "Error trimming video", e)
                throw e
            } finally {
                try {
                    muxer?.stop()
                    muxer?.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping muxer", e)
                }
                extractor.release()
            }
        }
    }
}
