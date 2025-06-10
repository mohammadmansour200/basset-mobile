package com.basset.operations.presentation.cut_operation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.basset.core.presentation.utils.formatDuration
import com.basset.core.presentation.utils.isValidDurationFormat
import com.basset.core.presentation.utils.parseDuration
import com.basset.operations.presentation.cut_operation.CutOperationAction
import com.basset.operations.presentation.cut_operation.CutOperationState

@Composable
fun RangeSelectorField(
    duration: Long,
    onAction: (CutOperationAction) -> Unit,
    state: CutOperationState
) {
    val startRangeTime by remember(state.startRangeProgress, duration) {
        derivedStateOf { duration * state.startRangeProgress }
    }
    val endRangeTime by remember(state.endRangeProgress, duration) {
        derivedStateOf { duration * state.endRangeProgress }
    }

    Row(
        modifier = Modifier
            .padding(top = 8.dp)

    ) {
        TimeTextField(
            time = startRangeTime,
            onTimeChange = { newTime ->
                onAction(
                    CutOperationAction.OnStartRangeChange(newTime / duration)
                )
            },
            duration = duration
        )
        Spacer(modifier = Modifier.padding(5.dp))
        TimeTextField(
            time = endRangeTime,
            onTimeChange = { newTime ->
                onAction(
                    CutOperationAction.OnEndRangeChange(newTime / duration)
                )
            },
            duration = duration
        )
    }
}

@Composable
private fun TimeTextField(
    time: Float,
    onTimeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    duration: Long
) {
    var text by remember { mutableStateOf(time.toDouble().formatDuration()) }
    LaunchedEffect(time) {
        text = time.toDouble().formatDuration()
    }

    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            if (newText.isValidDurationFormat()) {
                onTimeChange(newText.parseDuration().toFloat())
            }
        },
        isError = !text.isValidDurationFormat() || text.parseDuration() > duration,
        singleLine = true,
        modifier = modifier.width(100.dp)
    )
}