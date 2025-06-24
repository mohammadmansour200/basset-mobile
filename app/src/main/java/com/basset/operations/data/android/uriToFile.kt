package com.basset.operations.data.android

import android.content.Context
import android.net.Uri
import android.util.Log
import com.basset.core.utils.MimeTypeMap
import java.io.File

fun Context.uriToFile(uri: Uri) = with(contentResolver) {
    val extension = getUriExtension(uri)
    val outputFile = File(
        cacheDir.path,
        "amplituda.$extension"
    )
    openInputStream(uri)?.use { inputStream ->
        outputFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    Log.d("uriToFile", outputFile.name)
    return@with outputFile
}

fun Context.getUriExtension(uri: Uri): String? {
    val type = contentResolver.getType(uri) ?: return null
    return MimeTypeMap.getExtensionFromMimeType(type)
}