package com.basset.operations.presentation.cut_operation.components

import androidx.annotation.OptIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState

@OptIn(UnstableApi::class)
@Composable
fun PlayPauseButton(player: Player) {
    val state = rememberPlayPauseButtonState(player)

    AnimatedPlayPauseButton(
        isPlaying = !state.showPlay,
        onClick = state::onClick,
        iconColor = MaterialTheme.colorScheme.onSurface,
    )
}
