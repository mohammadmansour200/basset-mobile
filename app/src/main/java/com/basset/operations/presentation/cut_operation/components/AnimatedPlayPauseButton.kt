package com.basset.operations.presentation.cut_operation.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.basset.R
import com.basset.core.presentation.components.IconWithTooltip

@Composable
fun AnimatedPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconColor: Color,
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
        ),
        label = "play_pause_morph"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        val text =
            if (!isPlaying) stringResource(R.string.playpause_button_play)
            else stringResource(R.string.playpause_button_pause)
        IconWithTooltip(
            content = {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawAnimatedPlayPause(animationProgress, iconColor, isPlaying)
                }
            },
            text = text,
        )
    }
}

private fun DrawScope.drawAnimatedPlayPause(
    progress: Float,
    color: Color, isPlayToPause: Boolean
) {
    val paint = Paint().apply {
        this.color = color
        style = PaintingStyle.Fill
        isAntiAlias = true
    }

    drawContext.canvas.save()

    // Center translation with slight offset
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val offsetX = 1.dp.toPx() * (1f - progress) // Telegram's offset logic

    drawContext.canvas.translate(centerX + offsetX, centerY)

    // Rotation calculation
    val rotation = calculateRotation(
        progress,
        isPlayToPause
    )
    if (progress < 0.5f) {
        drawContext.canvas.rotate(rotation)
    }

    // Draw the morphing shape
    drawPlayPauseShape(progress, paint)

    // Draw again vertically flipped
    drawContext.canvas.scale(1f, -1f)
    drawPlayPauseShape(progress, paint)

    drawContext.canvas.restore()
}

private fun calculateRotation(progress: Float, isPlayToPause: Boolean): Float {
    val ms = 500f * progress

    return if (isPlayToPause) {
        // Play to Pause transition
        val adjustedMs = ms + 100f
        when {
            adjustedMs < 384f -> 95f * easeInOut(adjustedMs / 384f)
            adjustedMs < 484f -> 95f - 5f * easeInOut((adjustedMs - 384f) / 100f)
            else -> 90f
        }
    } else {
        // Pause to Play transition
        when {
            ms < 100f -> -5f * easeInOut(ms / 100f)
            ms < 484f -> -5f + 95f * easeInOut((ms - 100f) / 384f)
            else -> 90f
        }
    }
}

private fun easeInOut(t: Float): Float {
    return if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
}

private fun DrawScope.drawPlayPauseShape(progress: Float, paint: Paint) {
    val shapeSize = size.width * 0.3f

    if (progress < 0.5f) {
        // Draw play triangle
        val path = Path().apply {
            moveTo(-shapeSize / 3f, -shapeSize / 2f)
            lineTo(shapeSize / 2f, 0f)
            lineTo(-shapeSize / 3f, shapeSize / 2f)
            close()
        }
        drawPath(
            path = path,
            color = paint.color,
            colorFilter = paint.colorFilter,
            blendMode = paint.blendMode,
            alpha = paint.alpha
        )
    } else {
        // Draw pause rectangles
        val rectWidth = shapeSize * 0.25f
        val rectHeight = shapeSize * 0.9f
        val spacing = shapeSize * 0.15f

        // Left rectangle
        drawRect(
            color = paint.color,
            topLeft = Offset(-spacing - rectWidth, -rectHeight / 2f),
            size = Size(rectWidth, rectHeight)
        )

        // Right rectangle
        drawRect(
            color = paint.color,
            topLeft = Offset(spacing, -rectHeight / 2f),
            size = Size(rectWidth, rectHeight)
        )
    }
}