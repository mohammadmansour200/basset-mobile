package com.basset.operations.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.basset.ml.BgRemoval
import com.basset.operations.domain.BackgroundRemover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class TFLiteBackgroundRemover(private val context: Context) : BackgroundRemover {
    override suspend fun processImage(
        bitmap: Bitmap
    ): Pair<Bitmap, Bitmap> = withContext(Dispatchers.Default) {
        val model = BgRemoval.newInstance(context)

        val preprocessedImage = preprocessImage(bitmap)

        // Run inference
        val outputs = model.process(preprocessedImage)
        val maskOutputs = outputs.segmentationMasksAsTensorBuffer

        val postprocessedImage = postprocessImage(bitmap, maskOutputs.buffer)

        model.close()
        return@withContext postprocessedImage
    }

    private fun preprocessImage(bitmap: Bitmap): TensorBuffer {
        // Preprocessing - resize to 512x512
        val inputImageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(512, 512, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val inputTensorImage = TensorImage(DataType.FLOAT32)
        inputTensorImage.load(bitmap)
        val processedInputImage = inputImageProcessor.process(inputTensorImage)

        val inputImageByteBuffer = processedInputImage.buffer
        val image = TensorBuffer.createFixedSize(intArrayOf(1, 512, 512, 3), DataType.FLOAT32)
        image.loadBuffer(inputImageByteBuffer)
        return image
    }

    private fun postprocessImage(
        originalBitmap: Bitmap,
        maskBuffer: ByteBuffer
    ): Pair<Bitmap, Bitmap> {
        maskBuffer.rewind()
        // Postprocessing - get output image
        // Read the 512x512 mask data
        val maskValues = FloatArray(512 * 512)
        for (i in 0 until 512 * 512) {
            maskValues[i] = maskBuffer.float
        }

        // Create a scaled mask bitmap to match original dimensions
        val maskPixels = IntArray(512 * 512)
        for (i in maskValues.indices) {
            val alpha = (maskValues[i] * 255).toInt().coerceIn(0, 255)
            maskPixels[i] = Color.argb(alpha, 255, 255, 255) // White with varying alpha
        }

        val maskBitmap = Bitmap.createBitmap(maskPixels, 512, 512, Bitmap.Config.ARGB_8888)
        val scaledMask =
            Bitmap.createScaledBitmap(maskBitmap, originalBitmap.width, originalBitmap.height, true)

        val backgroundResult = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val backgroundCanvas = Canvas(backgroundResult)
        backgroundCanvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val backgroundPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
        backgroundCanvas.drawBitmap(scaledMask, 0f, 0f, backgroundPaint)

        val foregroundResult = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val foregroundCanvas = Canvas(foregroundResult)
        foregroundCanvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val foregroundPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        foregroundCanvas.drawBitmap(scaledMask, 0f, 0f, foregroundPaint)

        // Clean up
        maskBitmap.recycle()
        scaledMask.recycle()

        return Pair(backgroundResult, foregroundResult)
    }

}
