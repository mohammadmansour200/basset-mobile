package com.basset.operations.domain.model


enum class Format {
    MP4, MP3, AAC, OGG, WEBM, PNG, JPEG, WEBP
}

data class OutputFileInfo(val extension: String, val name: String?) {
    val format: Format = when (extension) {
        "mp4" -> Format.MP4
        "mp3" -> Format.MP3
        "aac" -> Format.AAC
        "ogg" -> Format.OGG
        "webm" -> Format.WEBM
        "png" -> Format.PNG
        "jpeg" -> Format.JPEG
        else -> {
            Format.WEBP
        }
    }
}