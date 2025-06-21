package com.basset.operations.domain.model

enum class Format {
    // Video formats
    MP4,
    WEBM,

    // Audio formats
    MP3,
    AAC,
    OGG,

    // Image formats
    PNG,
    JPEG,
    WEBP;

    fun isVideo(): Boolean = this == MP4 || this == WEBM
    fun isAudio(): Boolean = this == MP3 || this == AAC || this == OGG
    fun isImage(): Boolean = this == PNG || this == JPEG || this == WEBP
}