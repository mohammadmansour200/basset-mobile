package com.basset.operations.domain.model

enum class Format {
    // Video formats
    MP4,
    WEBM,

    // Audio formats
    MP3,
    AAC,
    OGA,

    // Image formats
    PNG,
    JPEG,

    // Had to add this or else Intent for jpgs will crash the app
    JPG,
    WEBP;

    fun isVideo(): Boolean = this == MP4 || this == WEBM
    fun isAudio(): Boolean = this == MP3 || this == AAC || this == OGA
    fun isImage(): Boolean = this == PNG || this == JPEG || this == WEBP || this == JPG
}