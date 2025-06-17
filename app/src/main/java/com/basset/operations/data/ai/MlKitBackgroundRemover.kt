package com.basset.operations.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.basset.operations.domain.BackgroundRemover
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MlKitBackgroundRemover(private val context: Context) : BackgroundRemover {
    private val segmenter = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
    )


    override suspend fun processImage(
        uri: Uri,
    ): Bitmap = suspendCoroutine { cont ->
        val inputImage = InputImage.fromFilePath(context, uri)
        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                val bitmap = result.foregroundBitmap
                if (bitmap != null) {
                    cont.resume(bitmap)
                } else {
                    cont.resumeWithException(Exception("Failed to extract foreground"))
                }
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }
}