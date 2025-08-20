package com.basset.operations.presentation

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.basset.operations.domain.model.Metadata
import com.basset.operations.domain.model.OperationError

@Immutable
data class OperationScreenState(
    val isOperating: Boolean = false,
    val progress: Float? = null,
    val outputedFile: Uri? = null,
    val metadata: Metadata = Metadata(),
    val outputFilename: String? = null,
    val outputAuthor: String? = null,
    val outputAlbumArt: Uri? = null,
    val outputedFileMetadata: Metadata = Metadata(),
    val operationError: OperationError? = null,
    val detailedErrorMessage: String? = null
)
