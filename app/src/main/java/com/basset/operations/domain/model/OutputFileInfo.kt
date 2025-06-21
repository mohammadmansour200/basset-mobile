package com.basset.operations.domain.model

data class OutputFileInfo(val extension: String, val name: String?) {
    val format: Format = Format.valueOf(extension.uppercase())
}
