package com.basset.operations.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.basset.core.domain.model.OperationType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState
import com.basset.operations.presentation.cut_operation.components.CutOperation

@Composable
fun OperationScreenContent(
    pickedFile: OperationRoute,
    accentColor: Color,
    snackbarHostState: SnackbarHostState,
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState
) {
    when (pickedFile.operationType) {
        OperationType.COMPRESS -> TODO()
        OperationType.CONVERT -> TODO()
        OperationType.BG_REMOVE -> BgRemoveOperation(
            onAction = { onAction(it) },
            operationScreenState = operationScreenState
        )

        OperationType.CUT -> CutOperation(
            pickedFile = pickedFile,
            accentColor = accentColor,
            snackbarHostState = snackbarHostState,
            operationScreenState = operationScreenState,
            onAction = { onAction(it) },
        )
    }
}