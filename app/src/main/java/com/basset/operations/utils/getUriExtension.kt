package com.basset.operations.utils

import android.content.Context
import android.net.Uri
import com.basset.core.utils.MimeTypeMap

fun Context.getUriExtension(uri: Uri): String? {
    val type = contentResolver.getType(uri) ?: return null
    return MimeTypeMap.getExtensionFromMimeType(type)
}