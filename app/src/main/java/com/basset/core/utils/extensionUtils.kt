package com.basset.core.utils

fun String.isVideo(): Boolean {
    if (this.contains("/")) return this.contains("video")

    val mimeType = MimeTypeMap.getMimeTypeFromExtension(this)!!
    return mimeType.contains("video")
}

fun String.isAudio(): Boolean {
    if (this.contains("/")) return this.contains("audio")

    val mimeType = MimeTypeMap.getMimeTypeFromExtension(this)!!
    return mimeType.contains("audio")
}

fun String.isImage(): Boolean {
    if (this.contains("/")) return this.contains("image")

    val mimeType = MimeTypeMap.getMimeTypeFromExtension(this)!!
    return mimeType.contains("image")
}

fun String.isPdf(): Boolean {
    if (this.contains("/")) return this.contains("pdf")

    val mimeType = MimeTypeMap.getMimeTypeFromExtension(this)!!
    return mimeType.contains("pdf")
}