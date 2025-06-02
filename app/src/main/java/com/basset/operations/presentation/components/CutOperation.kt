package com.basset.operations.presentation.components

import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
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
import com.basset.operations.presentation.CutOperationMediaPlayerAction
import com.basset.operations.presentation.CutOperationMediaPlayerViewModel
import com.basset.operations.presentation.MAX_VIDEO_PREVIEW_IMAGES
import com.linc.audiowaveform.AudioWaveform
import org.koin.androidx.compose.koinViewModel

@OptIn(UnstableApi::class)
@Composable
fun CutOperation(
    pickedFile: OperationRoute,
    accentColor: Color
) {
    val viewModel = koinViewModel<CutOperationMediaPlayerViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(pickedFile.uri) {
        viewModel.onAction(
            CutOperationMediaPlayerAction.OnLoadMedia(
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
    Box(
        modifier = Modifier
            .widthIn(max = 500.dp)
    ) {
        when (pickedFile.mimeType) {
            MimeType.AUDIO -> AudioWaveform(
                amplitudes = state.amplitudes,
                progress = state.position.toFloat() / viewModel.player.duration.toFloat(),
                onProgressChange = {
                    viewModel.onAction(
                        CutOperationMediaPlayerAction.OnUpdateProgress(
                            it
                        )
                    )
                },
                spikeWidth = 2.dp,
                waveformBrush = SolidColor(Color.Gray),
                progressBrush = SolidColor(MaterialTheme.colorScheme.primary),
            )

            else ->
                Column {
                    for (i in 0 until MAX_VIDEO_PREVIEW_IMAGES) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.videoFrames.getOrNull(i))
                                .crossfade(true)
                                .build(),
                            contentDescription = null
                        )
                    }
                }
        }

    }

}