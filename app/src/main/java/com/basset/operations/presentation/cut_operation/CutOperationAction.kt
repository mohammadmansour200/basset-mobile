package com.basset.operations.presentation.cut_operation

import android.net.Uri
import com.basset.core.domain.model.MediaType

sealed interface CutOperationAction {
    data class OnUpdateProgress(val progress: Float) : CutOperationAction
    data class OnLoadMedia(val uri: Uri, val mediaType: MediaType) : CutOperationAction
    data class OnStartRangeChange(val position: Float) : CutOperationAction
    data class OnEndRangeChange(val position: Float) : CutOperationAction
}