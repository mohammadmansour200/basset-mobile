package com.basset.operations.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arthenica.ffmpegkit.FFmpegKit
import com.basset.R
import com.basset.operations.presentation.OperationScreenState
import kotlinx.coroutines.launch

@Composable
fun ExecuteOperationBtn(
    modifier: Modifier = Modifier,
    buttonLabel: String,
    onAction: () -> Unit,
    operationScreenState: OperationScreenState
) {
    val scope = rememberCoroutineScope()
    Spacer(Modifier.padding(top = 10.dp))
    AnimatedContent(
        targetState = operationScreenState.isOperating,
        label = "execute_operation_btn_animation"
    ) { isOperating ->
        if (!isOperating) {
            Button(
                onClick = { onAction() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (operationScreenState.progress != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.weight(1f),
                            progress = { operationScreenState.progress })
                        Text(
                            text = "${(operationScreenState.progress * 100).toInt()}%",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            FFmpegKit.cancel()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel_operation_label))
                }
            }
        }
    }
}
