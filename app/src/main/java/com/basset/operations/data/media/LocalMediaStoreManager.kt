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

class LocalMediaStoreManager(private val context: Context) : MediaStoreManager {
    private val contentResolver: ContentResolver = context.contentResolver

    override fun createMediaUri(pickedFile: OperationRoute, outputFileInfo: OutputFileInfo): Uri? {
        val collectionUri: Uri
        val contentValues = ContentValues()
        val outputName = outputFileInfo.name ?: pickedFile.uri.toUri().getFileName(context)

        when (pickedFile.mimeType) {
            MimeType.AUDIO -> {
                collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
            }

            MimeType.IMAGE -> {
                collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                contentValues.put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "$outputName.${outputFileInfo.extension}"
                )
                contentValues.put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/${outputFileInfo.extension}"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
                }
            }

            MimeType.VIDEO -> {
                collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        return contentResolver.insert(collectionUri, contentValues)
    }

    override fun saveMedia(
        uri: Uri,
        pickedFile: OperationRoute,
    ) {
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

    override fun writeBitmap(
        uri: Uri,
        outputFileInfo: OutputFileInfo,
        bitmap: Bitmap
    ) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val format = when (outputFileInfo.format) {
                    Format.JPEG -> Bitmap.CompressFormat.JPEG
                    Format.PNG -> Bitmap.CompressFormat.PNG
                    else -> {
                        Bitmap.CompressFormat.WEBP
                    }
                }
                bitmap.compress(format, 100, outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun deleteMedia(uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }
}
