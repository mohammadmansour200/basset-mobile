package com.basset.operations.presentation

import com.basset.core.navigation.OperationRoute

sealed interface OperationScreenAction {
    data class OnCut(val pickedFile: OperationRoute, val start: Double, val end: Double) :
        OperationScreenAction

    data class OnCompress(val pickedFile: OperationRoute, val compressionRate: Int) :
        OperationScreenAction

    data class OnConvert(val pickedFile: OperationRoute, val outputFormat: String) :
        OperationScreenAction

    data class OnRemoveBackground(val pickedFile: OperationRoute) : OperationScreenAction
}