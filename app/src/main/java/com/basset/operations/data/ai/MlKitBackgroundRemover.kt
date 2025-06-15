package com.basset.operations.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.basset.operations.domain.BackgroundRemover
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions

class MlKitBackgroundRemover(private val context: Context) : BackgroundRemover {
    private val segmenter = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
    )


    override suspend fun processImage(
        uri: Uri,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val inputImage = InputImage.fromFilePath(context, uri)

        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                // Extract foreground bitmap with transparency
                val foregroundBitmap = result.foregroundBitmap
                if (foregroundBitmap != null) {
                    onSuccess(foregroundBitmap)
                } else {
                    onFailure(Exception("Failed to extract foreground"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}