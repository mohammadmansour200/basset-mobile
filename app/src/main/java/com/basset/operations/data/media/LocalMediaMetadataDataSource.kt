package com.basset.operations.data.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.basset.core.domain.model.MediaType
import com.basset.operations.data.android.getFileName
import com.basset.operations.data.android.getUriExtension
import com.basset.operations.domain.MediaMetadataDataSource
import com.basset.operations.domain.model.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaMetadataDataSource(
    private val context: Context
) : MediaMetadataDataSource {

    override suspend fun loadMetadata(uri: Uri, mediaType: MediaType): Metadata =
        withContext(Dispatchers.IO) {
            val fileSizeBytes = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
                }
            val ext = context.getUriExtension(uri)
            if (mediaType == MediaType.IMAGE) {
                return@withContext Metadata(
                    title = uri.getFileName(context),
                    fileSizeBytes = fileSizeBytes,
                    artist = null,
                    durationMs = null,
                    imageData = uri,
                    ext = ext
                )
            }

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.getFileName(context)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val durationMs =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
            val imageData = when (mediaType) {
                MediaType.VIDEO -> retriever.getFrameAtTime(0)
                MediaType.AUDIO -> retriever.embeddedPicture
                else -> null
            }

            retriever.release()
            Metadata(title, artist, durationMs, fileSizeBytes, imageData, ext)
        }

    override suspend fun loadCompactMetadata(
        uri: Uri,
    ): Metadata = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()

        val fileSizeBytes = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
            }
        val ext = context.getUriExtension(uri)

        retriever.setDataSource(context, uri)

        val durationMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()

        retriever.release()
        Metadata(
            title = "",
            fileSizeBytes = fileSizeBytes,
            artist = null,
            durationMs = durationMs,
            imageData = null,
            ext = ext
        )
    }
}