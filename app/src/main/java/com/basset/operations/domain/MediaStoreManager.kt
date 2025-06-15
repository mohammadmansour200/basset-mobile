package com.basset.operations.domain

import android.graphics.Bitmap
import android.net.Uri
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.model.OutputFileInfo

interface MediaStoreManager {
    fun createMediaUri(pickedFile: OperationRoute, outputFileInfo: OutputFileInfo): Uri?
    fun saveMedia(
        uri: Uri,
        pickedFile: OperationRoute,
    )

    fun writeBitmap(
        uri: Uri,
        outputFileInfo: OutputFileInfo,
        bitmap: Bitmap
    )

    fun deleteMedia(uri: Uri)
}