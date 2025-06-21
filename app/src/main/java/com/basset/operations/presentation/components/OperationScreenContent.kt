package com.basset.operations.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.basset.core.domain.model.OperationType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState
import com.basset.operations.presentation.cut_operation.CutOperation

@Composable
fun OperationScreenContent(
    pickedFile: OperationRoute,
    snackbarHostState: SnackbarHostState,
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState
) {
    when (pickedFile.operationType) {
        OperationType.COMPRESS -> CompressOperation(
            onAction = { onAction(it) },
            operationScreenState = operationScreenState,
            snackbarHostState = snackbarHostState,
            pickedFile = pickedFile
        )

        OperationType.CONVERT -> ConvertOperation(
            onAction = { onAction(it) },
            operationScreenState = operationScreenState,
            snackbarHostState = snackbarHostState,
            pickedFile = pickedFile
        )

        OperationType.BG_REMOVE -> BgRemoveOperation(
            onAction = { onAction(it) },
            operationScreenState = operationScreenState,
            snackbarHostState = snackbarHostState
        )

        OperationType.CUT -> CutOperation(
            pickedFile = pickedFile,
            snackbarHostState = snackbarHostState,
            operationScreenState = operationScreenState,
            onAction = { onAction(it) },
        )
    }
}