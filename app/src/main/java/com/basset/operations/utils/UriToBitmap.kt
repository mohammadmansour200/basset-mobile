package com.basset.operations.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Uri.toBitmap(
    targetWidth: Int?,
    targetHeight: Int?,
    context: Context,
    scaled: Boolean = false
): Bitmap = withContext(Dispatchers.IO) {
    val uri = this@toBitmap
    val isScaled = scaled && targetWidth != null && targetHeight != null
    return@withContext if (Build.VERSION.SDK_INT < 28) {
        val input = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(input, null, this)
            input?.close()
            if (isScaled) {
                val sampleSizeW = outWidth / targetWidth
                val sampleSizeH = outHeight / targetHeight
                inSampleSize = maxOf(1, minOf(sampleSizeW, sampleSizeH))
            } else {
                inSampleSize = 1
            }
            inJustDecodeBounds = false
        }

        val input2 = context.contentResolver.openInputStream(uri)
        val decoded = BitmapFactory.decodeStream(input2, null, options)
        input2?.close()
        if (isScaled) {
            decoded!!.scale(targetWidth, targetHeight)
        } else {
            decoded!!
        }
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return@withContext ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
            if (isScaled) {
                decoder.setTargetSize(targetWidth, targetHeight)
            }
        }
    }
}
