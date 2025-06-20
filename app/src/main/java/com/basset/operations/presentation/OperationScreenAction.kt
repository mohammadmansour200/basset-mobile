package com.basset.operations.presentation

sealed interface OperationScreenAction {
    data class OnCut(val start: Double, val end: Double) :
        OperationScreenAction

    data class OnCompress(val compressionRate: Int) :
        OperationScreenAction

    data class OnConvert(val outputFormat: String) :
        OperationScreenAction

    data class OnRemoveBackground(val background: Any?) : OperationScreenAction
}