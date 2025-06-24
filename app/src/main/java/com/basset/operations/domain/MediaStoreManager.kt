package com.basset.operations.domain

import android.graphics.Bitmap
import android.net.Uri

interface MediaStoreManager {
    suspend fun createMediaUri(
        uri: Uri,
        name: String?,
        extension: String
    ): Result<Uri?>

    suspend fun saveMedia(uri: Uri)

    suspend fun writeBitmap(
        uri: Uri,
        extension: String,
        bitmap: Bitmap,
        quality: Int = 100
    ): Result<Unit>

    suspend fun deleteMedia(uri: Uri)
}