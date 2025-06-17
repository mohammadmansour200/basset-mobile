package com.basset.operations.domain

import android.graphics.Bitmap
import android.net.Uri

interface BackgroundRemover {
    suspend fun processImage(
        uri: Uri,
    ): Bitmap
}