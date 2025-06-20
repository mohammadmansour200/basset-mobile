package com.basset.operations.data.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale

fun Uri.toBitmap(targetWidth: Int, targetHeight: Int, context: Context): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        val input = context.contentResolver.openInputStream(this)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(input, null, this)
            input?.close()

            val sampleSizeW = outWidth / targetWidth
            val sampleSizeH = outHeight / targetHeight
            inSampleSize = maxOf(1, minOf(sampleSizeW, sampleSizeH))
            inJustDecodeBounds = false
        }

        val input2 = context.contentResolver.openInputStream(this)
        val decoded = BitmapFactory.decodeStream(input2, null, options)
        input2?.close()
        decoded!!.scale(targetWidth, targetHeight)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, this)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
            decoder.setTargetSize(targetWidth, targetHeight)
        }
    }
}
