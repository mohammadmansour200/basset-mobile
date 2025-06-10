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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
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
import com.basset.core.presentation.utils.formatDuration
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

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Text(state.position.formatDuration())
    }

    val progress = state.position.toFloat() / viewModel.player.duration.toFloat()
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
    ) {
        var boxYPosition by remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
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

                TimelineBars(
                    timelineWidthPx,
                    { viewModel.onAction(it) },
                    progress,
                    timelineHeight,
                    state
                )
            }
        }

        RangeSelectorField(
            duration = viewModel.player.duration,
            onAction = { viewModel.onAction(it) },
            state = state
        )
    }
}



