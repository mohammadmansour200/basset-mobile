package com.basset.operations.domain.model

data class Metadata(
    val title: String?,
    val artist: String?,
    val durationMs: Long?,
    val fileSizeBytes: Long?,
    val imageData: Any?
)