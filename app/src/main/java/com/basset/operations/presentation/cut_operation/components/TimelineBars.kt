package com.basset.operations.presentation.cut_operation.components


import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.basset.operations.presentation.cut_operation.CutOperationAction
import com.basset.operations.presentation.cut_operation.CutOperationState

@Composable
fun BoxWithConstraintsScope.TimelineBars(
    timelineWidthPx: Float,
    onAction: (
        action: CutOperationAction
    ) -> Unit,
    progress: Float,
    timelineHeight: Dp,
    state: CutOperationState
) {
    // Gestures bar
    Box(
        modifier = Modifier
            .matchParentSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    val tappedProgress = (it.x / timelineWidthPx).coerceIn(0f, 1f)
                    onAction(
                        CutOperationAction.OnUpdateProgress(tappedProgress)
                    )
                })
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val newProgress =
                        (progress + delta / timelineWidthPx).coerceIn(0f, 1f)

                    onAction(
                        CutOperationAction.OnUpdateProgress(newProgress)
                    )
                }
            )
    )

    // Progress indicator
    Box(
        modifier = Modifier
            .offset(x = minWidth.times(progress))
            .shadow(8.dp, RectangleShape)
            .width(2.dp)
            .background(MaterialTheme.colorScheme.primary)
            .height(timelineHeight)
    )

    // Start range bar
    Box(
        modifier = Modifier
            .offset(x = maxWidth.times(state.startRangeProgress) - 12.dp)
            .width(20.dp)
            .height(timelineHeight)
            .draggable2D(
                state = rememberDraggable2DState {
                    val newStartRange =
                        ((state.startRangeProgress * timelineWidthPx + it.x) / timelineWidthPx)
                            .coerceIn(0f, state.endRangeProgress)
                    onAction(
                        CutOperationAction.OnStartRangeChange(newStartRange)
                    )
                }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(4.dp)
                .height(timelineHeight)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                )
        )
    }


    // End range bar
    Box(
        modifier = Modifier
            .offset(x = maxWidth.times(state.endRangeProgress) - 8.dp)
            .width(20.dp)
            .height(timelineHeight)
            .draggable2D(
                state = rememberDraggable2DState {
                    val newEndRange =
                        ((state.endRangeProgress * timelineWidthPx + it.x) / timelineWidthPx)
                            .coerceIn(state.startRangeProgress, 1f)
                    onAction(
                        CutOperationAction.OnEndRangeChange(newEndRange)
                    )
                }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(4.dp)
                .height(timelineHeight)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                )
        )
    }

    Box(
        modifier = Modifier
            .offset(x = 0.dp)
            .width(minWidth.times(state.startRangeProgress))
            .height(timelineHeight)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
    )
    Box(
        modifier = Modifier
            .offset(x = minWidth.times(state.endRangeProgress))
            .width(minWidth.times(1f - state.endRangeProgress))
            .height(timelineHeight)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
    )
}


