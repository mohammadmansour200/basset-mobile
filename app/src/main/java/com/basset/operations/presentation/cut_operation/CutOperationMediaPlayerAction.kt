package com.basset.operations.presentation.cut_operation

import android.net.Uri
import com.basset.core.domain.model.MimeType

sealed interface CutOperationMediaPlayerAction {
    data class OnUpdateProgress(val progress: Float) : CutOperationMediaPlayerAction
    data class OnLoadMedia(val uri: Uri, val mimeType: MimeType) : CutOperationMediaPlayerAction
}