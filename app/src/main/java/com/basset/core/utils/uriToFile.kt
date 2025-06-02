package com.basset.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
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

fun ContentResolver.getUriExtension(uri: Uri): String? {
    val type = getType(uri) ?: return null
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
}