package com.basset.operations.domain.cut_operation

import android.graphics.Bitmap
import android.net.Uri

interface CuttingTimelineDataSource {
    suspend fun loadAmplitudes(uri: Uri): List<Int>
    suspend fun loadVideoPreviewFrames(uri: Uri, onThumbnailReady: (bitmap: Bitmap) -> Unit)
}