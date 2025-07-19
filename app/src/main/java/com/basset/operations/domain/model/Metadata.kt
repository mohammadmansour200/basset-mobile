package com.basset.operations.domain.model

data class Metadata(
    val title: String = "",
    val artist: String? = null,
    val durationMs: Long? = null,
    val fileSizeBytes: Long? = null,
    val imageData: Any? = null,
    val ext: String? = null,
    val bitrate: Long? = null
)