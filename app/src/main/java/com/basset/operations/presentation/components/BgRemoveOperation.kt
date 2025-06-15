package com.basset.operations.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState

@Composable
fun BgRemoveOperation(
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(operationScreenState.outputedFile)
            .crossfade(true)
            .build(),
        contentDescription = null,
    )

    ExecuteOperationBtn(
        buttonLabel = "Remove",
        onAction = { onAction(OperationScreenAction.OnRemoveBackground()) },
        operationScreenState = operationScreenState
    )
}