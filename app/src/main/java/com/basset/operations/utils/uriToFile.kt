package com.basset.operations.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

fun Context.uriToFile(uri: Uri) = with(contentResolver) {
    val extension = getUriExtension(uri)
    val outputFile = File(
        cacheDir.path,
        "temp.$extension"
    )
    openInputStream(uri)?.use { inputStream ->
        outputFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    Log.d("uriToFile", outputFile.name)
    return@with outputFile
}