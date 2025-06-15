package com.basset.operations.domain

import android.graphics.Bitmap
import android.net.Uri
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.model.OutputFileInfo

interface MediaStoreManager {
    suspend fun createMediaUri(pickedFile: OperationRoute, outputFileInfo: OutputFileInfo): Uri?
    suspend fun saveMedia(
        uri: Uri,
        pickedFile: OperationRoute,
    )

    suspend fun writeBitmap(
        uri: Uri,
        outputFileInfo: OutputFileInfo,
        bitmap: Bitmap
    )

    suspend fun deleteMedia(uri: Uri)
}