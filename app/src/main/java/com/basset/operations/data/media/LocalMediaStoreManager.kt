package com.basset.operations.data.media

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.basset.core.utils.MimeTypeMap
import com.basset.core.utils.isAudio
import com.basset.core.utils.isImage
import com.basset.core.utils.isPdf
import com.basset.core.utils.isVideo
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.utils.getFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class LocalMediaStoreManager(private val context: Context) : MediaStoreManager {
    private val contentResolver: ContentResolver = context.contentResolver
    private val isAndroidQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override suspend fun createMediaUri(
        uri: Uri,
        name: String?,
        extension: String
    ): Result<Uri?> =
        withContext(Dispatchers.IO) {
            val contentValues = ContentValues()
            val outputName = name ?: uri.getFileName(context)
            val displayName = "$outputName.$extension"
            val mimeType = MimeTypeMap.getMimeTypeFromExtension(extension)!!

            if (isAndroidQOrLater) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)

                when {
                    extension.isAudio() -> {
                        val collectionUri =
                            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        contentValues.put(
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            displayName
                        )
                        contentValues.put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                        contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music")

                        return@withContext Result.success(
                            contentResolver.insert(
                                collectionUri,
                                contentValues
                            )
                        )
                    }

                    extension.isImage() -> {
                        val collectionUri =
                            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        contentValues.put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            displayName
                        )
                        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")

                        return@withContext Result.success(
                            contentResolver.insert(
                                collectionUri,
                                contentValues
                            )
                        )
                    }

                    extension.isVideo() -> {
                        val collectionUri =
                            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        contentValues.put(
                            MediaStore.Video.Media.DISPLAY_NAME,
                            displayName
                        )
                        contentValues.put(
                            MediaStore.Video.Media.MIME_TYPE,
                            mimeType
                        )
                        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies")

                        return@withContext Result.success(
                            contentResolver.insert(
                                collectionUri,
                                contentValues
                            )
                        )
                    }

                    extension.isPdf() -> {
                        val collectionUri =
                            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        contentValues.put(
                            MediaStore.Files.FileColumns.DISPLAY_NAME,
                            displayName
                        )
                        contentValues.put(
                            MediaStore.Files.FileColumns.MIME_TYPE,
                            mimeType
                        )
                        contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents")

                        return@withContext Result.success(
                            contentResolver.insert(
                                collectionUri,
                                contentValues
                            )
                        )
                    }

                    else -> throw IllegalArgumentException("Invalid type")
                }
            } else {
                val directory = when {
                    extension.isAudio() -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    extension.isImage() -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    extension.isVideo() -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    extension.isPdf() -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    else -> null
                }
                val file = File(directory, displayName)
                file.createNewFile()

                return@withContext Result.success(
                    file.toUri()
                )
            }
        }

    override suspend fun saveMedia(
        uri: Uri
    ) =
        withContext(Dispatchers.IO) {
            if (isAndroidQOrLater) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, values, null, null)
            }
        }

    override suspend fun writeBitmap(
        uri: Uri,
        extension: String,
        bitmap: Bitmap,
        quality: Int
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val format = when (extension) {
                        "webp" -> Bitmap.CompressFormat.WEBP
                        "png" -> Bitmap.CompressFormat.PNG
                        else -> Bitmap.CompressFormat.JPEG
                    }
                    val success = bitmap.compress(format, quality, outputStream)
                    if (success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(IOException("Failed to compress bitmap to $uri"))
                    }
                }
                    ?: Result.failure(IOException("Could not open output stream for $uri")) // Handle null outputStream
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteMedia(uri: Uri): Unit =
        withContext(Dispatchers.IO) {
            if (isAndroidQOrLater) {
                context.contentResolver.delete(uri, null, null)
            } else {
                val file = File(uri.path!!)
                file.delete()
            }
        }
}
