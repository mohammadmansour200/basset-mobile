package com.basset.operations.presentation

import android.net.Uri
import com.basset.operations.domain.model.CompressionRate
import com.basset.operations.domain.model.Format

sealed interface OperationScreenAction {
    data class OnCut(val start: Double, val end: Double) :
        OperationScreenAction

    data class OnCompress(val compressionRate: CompressionRate) :
        OperationScreenAction

    data class OnConvert(val outputFormat: Format) :
        OperationScreenAction

    data class OnAudioToVideoConvert(val image: Uri, val outputFormat: Format) :
        OperationScreenAction

    data class OnRemoveBackground(val background: Any?) : OperationScreenAction
}