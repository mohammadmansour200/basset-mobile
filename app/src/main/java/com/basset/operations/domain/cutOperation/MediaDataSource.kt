package com.basset.operations.domain.cutOperation

import android.graphics.Bitmap
import android.net.Uri

interface MediaDataSource {
    suspend fun loadAmplitudes(uri: Uri): List<Int>
    suspend fun loadVideoPreviewFrames(uri: Uri, onThumbnailReady: (bitmap: Bitmap) -> Unit)
}