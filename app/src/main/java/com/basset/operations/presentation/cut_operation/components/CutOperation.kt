package com.basset.operations.presentation.cut_operation.components

import android.R
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.presentation.cut_operation.CutOperationAction
import com.basset.operations.presentation.cut_operation.CutOperationViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(
    UnstableApi::class, ExperimentalFoundationApi::class
)
@Composable
fun CutOperation(
    pickedFile: OperationRoute,
    accentColor: Color
) {
    val viewModel = koinViewModel<CutOperationViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(pickedFile.uri) {
        viewModel.onAction(
            CutOperationAction.OnLoadMedia(
                pickedFile.uri.toUri(),
                pickedFile.mimeType
            )
        )
    }

    if (pickedFile.mimeType == MimeType.VIDEO) Crossfade(viewModel.player.isCurrentMediaItemSeekable) {
        if (it) PlayerSurface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .aspectRatio(
                    16 / 9f
                )
                .clip(RoundedCornerShape(10.dp)),
            player = viewModel.player,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW
        ) else Box(
            modifier = Modifier
                .aspectRatio(
                    16 / 9f
                )
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        )
    }
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            PlayPauseButton(
                player = viewModel.player,
                accentColor = accentColor
            )
            PlaybackSpeedPopUpButton(
                player = viewModel.player,
                accentColor = accentColor
            )
        }
    }

    val progress = state.position.toFloat() / viewModel.player.duration.toFloat()
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
    ) {
        var boxYPosition by remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .onGloballyPositioned { coordinates ->
                    boxYPosition = coordinates.positionInWindow().y
                },
            contentAlignment = Alignment.Center
        ) {
            TimelineVisualization(pickedFile, state, progress)

            BoxWithConstraints(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize()
            ) {
                val density = LocalDensity.current
                val timelineWidthPx =
                    with(density) { this@BoxWithConstraints.minWidth.toPx() }

                val timelineHeight = 62.dp

                val activity = LocalActivity.current
                LaunchedEffect(Unit) {
                    activity?.let { act ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val timelineHeightPx =
                                with(density) { timelineHeight.toPx().toInt() }

                            val timelineYPosition = boxYPosition.toInt()

                            val exclusionRects = listOf(
                                Rect(
                                    0,
                                    timelineYPosition,
                                    act.resources.displayMetrics.widthPixels,
                                    timelineYPosition + timelineHeightPx
                                ),
                            )
                            act.findViewById<View>(R.id.content).systemGestureExclusionRects =
                                exclusionRects
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                val tappedProgress = (it.x / timelineWidthPx).coerceIn(0f, 1f)
                                viewModel.onAction(
                                    CutOperationAction.OnUpdateProgress(tappedProgress)
                                )
                            })
                        }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                val newProgress =
                                    (progress + delta / timelineWidthPx).coerceIn(0f, 1f)

                                viewModel.onAction(
                                    CutOperationAction.OnUpdateProgress(newProgress)
                                )
                            }
                        )
                )

                Box(
                    modifier = Modifier
                        .offset(x = minWidth.times(progress))
                        .shadow(8.dp, RectangleShape)
                        .width(2.dp)
                        .background(Color.White)
                        .height(timelineHeight)
                )

                Box(
                    modifier = Modifier
                        .offset(x = maxWidth.times(state.startRangeProgress) - 12.dp)
                        .width(20.dp)
                        .height(timelineHeight)
                        .pointerInput(Unit) {
                            detectDragGestures { _, change ->
                                val newStartRange =
                                    ((state.startRangeProgress * timelineWidthPx + change.x) / timelineWidthPx)
                                        .coerceIn(0f, state.endRangeProgress)
                                viewModel.onAction(
                                    CutOperationAction.OnStartRangeChange(newStartRange)
                                )
                            }
                        }
                ) {
                    // Visual handle (narrow)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(4.dp) // Narrow visual handle
                            .height(timelineHeight)
                            .background(
                                Color.White,
                                RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = maxWidth.times(state.endRangeProgress) - 8.dp)
                        .width(20.dp)
                        .height(timelineHeight)
                        .pointerInput(Unit) {
                            detectDragGestures { _, change ->
                                val newEndRange =
                                    ((state.endRangeProgress * timelineWidthPx + change.x) / timelineWidthPx)
                                        .coerceIn(state.startRangeProgress, 1f)
                                viewModel.onAction(
                                    CutOperationAction.OnEndRangeChange(newEndRange)
                                )
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(4.dp)
                            .height(timelineHeight)
                            .background(
                                Color.White,
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
        }
    }
}



