package com.basset.operations.data.media

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.data.android.getFileName
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.domain.model.Format
import com.basset.operations.domain.model.OutputFileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaStoreManager(private val context: Context) : MediaStoreManager {
    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun createMediaUri(
        pickedFile: OperationRoute,
        outputFileInfo: OutputFileInfo
    ): Uri? =
        withContext(Dispatchers.IO) {
            val contentValues = ContentValues()
            val outputName = outputFileInfo.name ?: pickedFile.uri.toUri().getFileName(context)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            if (outputFileInfo.format.isAudio()) {
                val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                contentValues.put(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    "$outputName.${outputFileInfo.extension}"
                )
                if (outputFileInfo.format == Format.MP3) {
                    contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                } else {
                    contentValues.put(
                        MediaStore.Audio.Media.MIME_TYPE,
                        "audio/${outputFileInfo.extension}"
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music")
                }
                return@withContext contentResolver.insert(collectionUri, contentValues)
            }

            if (outputFileInfo.format.isImage()) {
                val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                contentValues.put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "$outputName.${outputFileInfo.extension}"
                )
                if (outputFileInfo.format == Format.JPG) {
                    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                } else {
                    contentValues.put(
                        MediaStore.Images.Media.MIME_TYPE,
                        "image/${outputFileInfo.extension}"
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
                }

                return@withContext contentResolver.insert(collectionUri, contentValues)
            }

            if (outputFileInfo.format.isVideo()) {
                val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                contentValues.put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    "$outputName.${outputFileInfo.extension}"
                )
                contentValues.put(
                    MediaStore.Video.Media.MIME_TYPE,
                    "video/${outputFileInfo.extension}"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies")
                }
                return@withContext contentResolver.insert(collectionUri, contentValues)
            } else return@withContext null

        }

    override suspend fun saveMedia(
        uri: Uri,
        pickedFile: OperationRoute,
    ) =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    when (pickedFile.mimeType) {
                        MimeType.AUDIO -> put(MediaStore.Audio.Media.IS_PENDING, 0)
                        MimeType.IMAGE -> put(MediaStore.Images.Media.IS_PENDING, 0)
                        MimeType.VIDEO -> put(MediaStore.Video.Media.IS_PENDING, 0)
                    }
                }
                context.contentResolver.update(uri, values, null, null)
            }
        }

    override suspend fun writeBitmap(
        uri: Uri,
        outputFileInfo: OutputFileInfo,
        bitmap: Bitmap,
        quality: Int
    ): Unit =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val format = when (outputFileInfo.format) {
                        Format.JPEG -> Bitmap.CompressFormat.JPEG
                        Format.JPG -> Bitmap.CompressFormat.JPEG
                        Format.PNG -> Bitmap.CompressFormat.PNG
                        else -> {
                            Bitmap.CompressFormat.WEBP
                        }
                    }
                    bitmap.compress(format, quality, outputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    override suspend fun deleteMedia(uri: Uri): Unit =
        withContext(Dispatchers.IO) {
            context.contentResolver.delete(uri, null, null)
        }
}
