package com.basset.operations.presentation.cut_operation.components

import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.MediaConstants.MAX_VIDEO_PREVIEW_IMAGES
import com.basset.operations.domain.MediaConstants.VIDEO_FRAME_INTERVAL_PERCENTAGE
import com.basset.operations.presentation.cut_operation.CutOperationAction
import com.basset.operations.presentation.cut_operation.CutOperationViewModel
import com.linc.audiowaveform.AudioWaveform
import org.koin.androidx.compose.koinViewModel

@OptIn(UnstableApi::class)
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
    val progress = state.position.toFloat() / viewModel.player.duration.toFloat()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            when (pickedFile.mimeType) {
                MimeType.AUDIO -> AudioWaveform(
                    amplitudes = state.amplitudes,
                    progress = progress,
                    onProgressChange = { /* Already implemented manually */ },
                    spikeWidth = 2.dp,
                    waveformBrush = SolidColor(Color.Gray),
                    progressBrush = SolidColor(MaterialTheme.colorScheme.primary),
                )

                else ->
                    Row {
                        for (i in 0 until MAX_VIDEO_PREVIEW_IMAGES) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(state.videoFrames.getOrNull(i))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(VIDEO_FRAME_INTERVAL_PERCENTAGE.toFloat())
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize()
            ) {
                val maxWidthPx =
                    with(LocalDensity.current) { this@BoxWithConstraints.maxWidth.toPx() }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                val tappedProgress = (it.x / maxWidthPx).coerceIn(0f, 1f)
                                viewModel.onAction(
                                    CutOperationAction.OnUpdateProgress(tappedProgress)
                                )
                            })
                        }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                val newProgress =
                                    (progress + delta / maxWidthPx).coerceIn(0f, 1f)

                                viewModel.onAction(
                                    CutOperationAction.OnUpdateProgress(newProgress)
                                )
                            }
                        )
                )

                Box(
                    modifier = Modifier
                        .offset(x = maxWidth.times(progress))
                        .shadow(8.dp, RectangleShape)
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .height(62.dp)
                )
            }
        }
    }
}