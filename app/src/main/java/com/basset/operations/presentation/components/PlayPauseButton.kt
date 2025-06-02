package com.basset.operations.presentation.components

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import com.basset.R
import com.basset.core.presentation.components.IconWithTooltip

@OptIn(UnstableApi::class)
@Composable
fun PlayPauseButton(player: Player, modifier: Modifier = Modifier, accentColor: Color) {
    val state = rememberPlayPauseButtonState(player)
    val icon = if (state.showPlay) Icons.Default.PlayArrow else Icons.Filled.PlayArrow
    val contentDescription =
        if (state.showPlay) stringResource(R.string.playpause_button_play)
        else stringResource(R.string.playpause_button_pause)
    IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {
        IconWithTooltip(icon, text = contentDescription, surfaceColor = accentColor)
    }
}