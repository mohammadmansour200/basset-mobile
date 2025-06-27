package com.basset.operations.domain

import android.graphics.Bitmap

interface BackgroundRemover {
    suspend fun processImage(
        bitmap: Bitmap,
    ): Pair<Bitmap, Bitmap>
}