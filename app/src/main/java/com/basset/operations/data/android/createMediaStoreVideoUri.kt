package com.basset.operations.data.android

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.Format
import com.basset.operations.domain.OutputFile

fun createMediaStoreUri(
    context: Context,
    pickedFile: OperationRoute,
    outputFile: OutputFile
): Uri? {
    val resolver = context.contentResolver

    val collectionUri: Uri
    val contentValues = ContentValues()
    val outputName = outputFile.name ?: pickedFile.uri.toUri().getFileName(context)

    when (pickedFile.mimeType) {
        MimeType.AUDIO -> {
            collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            contentValues.put(
                MediaStore.Audio.Media.DISPLAY_NAME,
                "$outputName.${outputFile.extension}"
            )
            if (outputFile.format == Format.MP3) {
                contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            } else {
                contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/${outputFile.extension}")
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
                "$outputName.${outputFile.extension}"
            )
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/${outputFile.extension}")
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
                "$outputName.${outputFile.extension}"
            )
            contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/${outputFile.extension}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies")
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    return resolver.insert(collectionUri, contentValues)
}

