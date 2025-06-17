package com.basset.operations.data.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.basset.operations.data.android.getFileName
import com.basset.operations.data.android.uriToFile
import com.basset.operations.domain.cut_operation.MediaConstants.MAX_VIDEO_PREVIEW_IMAGES
import com.basset.operations.domain.cut_operation.MediaConstants.VIDEO_FRAME_INTERVAL_PERCENTAGE
import com.basset.operations.domain.cut_operation.MediaDataSource
import com.linc.amplituda.Amplituda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class LocalMediaDataSource(
    private val appContext: Context
) : MediaDataSource {
    override suspend fun loadAmplitudes(uri: Uri): List<Int> = withContext(Dispatchers.IO) {
        var inputFile: File? = null
        try {
            val amplituda = Amplituda(appContext)
            inputFile = appContext.uriToFile(uri = uri)
            val result = amplituda.processAudio(inputFile).get()
            return@withContext result.amplitudesAsList() ?: emptyList()
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Unexpected error loading amplitudes: $uri", e)
            throw IOException("Error loading amplitudes", e)
        } finally {
            inputFile?.delete()?.also { deleted ->
                if (deleted) {
                    Log.d("MediaPlayer", "Temp file deleted: ${inputFile.name}")
                } else {
                    Log.w("MediaPlayer", "Failed to delete temp file: ${inputFile.name}")
                }
            }
        }
    }

    override suspend fun loadVideoPreviewFrames(uri: Uri, onThumbnailReady: (Bitmap) -> Unit) =
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(appContext, uri)

                val durationMs =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()

                Log.d(
                    "ThumbnailExtract",
                    "Extracting thumbnails for: ${uri.getFileName(appContext)} (URI: $uri)"
                )

                if (durationMs == null) {
                    Log.e("ThumbnailExtract", "Failed to read video duration for URI: $uri")
                    return@withContext
                }

                val interval = (VIDEO_FRAME_INTERVAL_PERCENTAGE * durationMs) / 100

                for (i in 0 until MAX_VIDEO_PREVIEW_IMAGES) {
                    val timeUs = i * interval * 1000
                    val bitmap =
                        retriever.getFrameAtTime(
                            timeUs.toLong(),
                            MediaMetadataRetriever.OPTION_CLOSEST
                        )
                    if (bitmap != null) onThumbnailReady(bitmap)
                }
            } catch (e: Exception) {
                throw IOException("Error loading video preview frames", e)
            } finally {
                retriever.release()
            }
        }
}