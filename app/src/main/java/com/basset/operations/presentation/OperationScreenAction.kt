package com.basset.operations.presentation

import android.net.Uri
import com.basset.operations.domain.model.Rate

sealed interface OperationScreenAction {
    data class OnCut(val start: Double, val end: Double) :
        OperationScreenAction

    data class OnCompress(val rate: Rate) :
        OperationScreenAction

    data class OnConvert(val outputExtension: String) :
        OperationScreenAction

    data class OnAudioToVideoConvert(val image: Uri, val outputExtension: String) :
        OperationScreenAction

    data class OnRemoveBackground(val background: Any?) : OperationScreenAction
}