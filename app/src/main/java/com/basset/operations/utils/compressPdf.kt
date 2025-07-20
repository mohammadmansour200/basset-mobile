package com.basset.operations.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

suspend fun compressPdf(context: Context, inputUri: Uri, outputUri: Uri, quality: Int) =
    withContext(Dispatchers.IO) {
        val pickedPdfFile = context.uriToFile(inputUri)
        val compressedPdfFile = File(context.cacheDir, "compressed.pdf")

        val imageCacheDir = File(context.cacheDir, "pdf_images")
        if (!imageCacheDir.exists()) {
            imageCacheDir.mkdirs()
        }

        val parcelFileDescriptor =
            ParcelFileDescriptor.open(pickedPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)

        val pdfWriter = PdfWriter(compressedPdfFile.absolutePath)
        val pdfDocument = PdfDocument(pdfWriter)

        for (pageIndex in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(pageIndex)

            // Create bitmap to render the page
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Compress to the lossless format: PNG
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val imageData = com.itextpdf.io.image.ImageDataFactory.create(imageBytes)

            // Create a page with dimensions matching the image
            val pageSize = PageSize(imageData.width, imageData.height)
            val pdfPage = pdfDocument.addNewPage(pageSize)

            // Use PdfCanvas for precise positioning
            val canvas = PdfCanvas(pdfPage)
            canvas.addImageFittedIntoRectangle(imageData, pageSize, false)

            // Clean up
            page.close()
            bitmap.recycle()
            byteArrayOutputStream.close()
        }

        // Image renderer clean up
        parcelFileDescriptor.close()
        pdfRenderer.close()
        pickedPdfFile.delete()

        // Itext PDF writer clean up
        pdfDocument.close()
        pdfWriter.close()

        // Write the compressed PDF to the output URI
        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            outputStream.write(compressedPdfFile.readBytes())
        }

        // Delete the temporary files
        compressedPdfFile.delete()
        imageCacheDir.deleteRecursively()
    }

