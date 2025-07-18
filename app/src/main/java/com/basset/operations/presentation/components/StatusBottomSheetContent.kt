package com.basset.operations.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.basset.core.domain.model.OperationType
import com.basset.operations.presentation.OperationScreenState

enum class StatusBottomSheetType {
    SUCCESS, ERROR
}

@Composable
fun StatusBottomSheetContent(
    modifier: Modifier = Modifier,
    type: StatusBottomSheetType,
    state: OperationScreenState,
    operationType: OperationType
) {
    when (type) {
        StatusBottomSheetType.SUCCESS -> SuccessBottomSheet(
            modifier = modifier,
            outputedFile = state.outputedFile,
            inputFileMetadata = state.metadata,
            outputedFileMetadata = state.outputedFileMetadata,
            operationType = operationType,
        )

        StatusBottomSheetType.ERROR -> ErrorBottomSheet(
            modifier = modifier,
            detailedErrorMessage = state.detailedErrorMessage,
            operationError = state.operationError
        )
    }
}