package com.basset.operations.presentation.cut_operation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.cutOperation.MediaConstants.MAX_VIDEO_PREVIEW_IMAGES
import com.basset.operations.domain.cutOperation.MediaConstants.VIDEO_FRAME_INTERVAL_PERCENTAGE
import com.basset.operations.presentation.cut_operation.CutOperationState
import com.linc.audiowaveform.AudioWaveform

@Composable
fun TimelineVisualization(
    pickedFile: OperationRoute,
    state: CutOperationState,
    progress: Float
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
}