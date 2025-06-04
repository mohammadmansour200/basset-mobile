package com.basset.operations.presentation.cut_operation.components

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlaybackSpeedState
import com.basset.R
import com.basset.core.presentation.components.IconWithTooltip

@OptIn(UnstableApi::class)
@Composable
fun PlaybackSpeedPopUpButton(
    player: Player,
    modifier: Modifier = Modifier,
    speedSelection: List<Float> = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f),
    accentColor: Color
) {
    val state = rememberPlaybackSpeedState(player)
    var openMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { openMenu = true }, modifier = modifier, enabled = state.isEnabled) {
        IconWithTooltip(
            icon = ImageVector.vectorResource(R.drawable.speed),
            text = stringResource(R.string.playback_speed),
            surfaceColor = accentColor,
        )
    }
    if (openMenu) {
        MenuOfChoices(
            currentSpeed = state.playbackSpeed,
            choices = speedSelection,
            onDismissRequest = { openMenu = false },
            onSelectChoice = state::updatePlaybackSpeed,
            menuExpanded = openMenu
        )
    }
}

@Composable
private fun MenuOfChoices(
    currentSpeed: Float,
    choices: List<Float>,
    menuExpanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectChoice: (Float) -> Unit,
) {
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = onDismissRequest,
    ) {

        choices.forEach { speed ->
            val isSelected = speed == currentSpeed
            var fontWeight = FontWeight(300)
            if (isSelected) {
                fontWeight = FontWeight(1000)
            }
            DropdownMenuItem(
                text = { Text("%.1fx".format(speed), fontWeight = fontWeight) },
                trailingIcon = { if (isSelected) Icon(imageVector = Icons.Filled.Check, null) },
                onClick = {
                    onSelectChoice(speed)
                    onDismissRequest()
                },
            )
        }
    }
}