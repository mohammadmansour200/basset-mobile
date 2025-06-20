package com.basset.core.presentation.modifier

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.basset.ui.theme.outlineVariant

fun Modifier.container(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    resultPadding: Dp = 4.dp,
    borderWidth: Dp = Dp.Unspecified,
    borderColor: Color? = null,
    autoShadowElevation: Dp = if (color != Color.Transparent) 1.dp else 0.dp,
    clip: Boolean = true,
    composeColorOnTopOfBackground: Boolean = true,
    shadowColor: Color = Color.Black
) = this.composed {
    val localContainerShape = LocalContainerShape.current
    val resultShape = localContainerShape ?: shape

    val colorScheme = MaterialTheme.colorScheme

    val containerColor = if (color.isUnspecified) {
        SafeLocalContainerColor
    } else {
        if (composeColorOnTopOfBackground) color.compositeOver(colorScheme.background)
        else color
    }

    val density = LocalDensity.current

    val genericModifier = Modifier.drawWithCache {
        val outline = resultShape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = density
        )
        onDrawWithContent {
            drawOutline(
                outline = outline,
                color = containerColor
            )
            if (borderWidth > 0.dp) {
                drawOutline(
                    outline = outline,
                    color = borderColor ?: colorScheme.outlineVariant(0.1f, containerColor),
                    style = Stroke(with(density) { borderWidth.toPx() })
                )
            }
            drawContent()
        }
    }

    val cornerModifier = Modifier
        .background(
            color = containerColor,
            shape = resultShape
        )
        .border(
            width = borderWidth,
            color = borderColor ?: colorScheme.outlineVariant(0.1f, containerColor),
            shape = resultShape
        )

    Modifier
        .shadow(
            shape = resultShape,
            elevation = animateDpAsState(
                if (borderWidth > 0.dp) {
                    0.dp
                } else autoShadowElevation.coerceAtLeast(0.dp)
            ).value,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .then(
            if (resultShape is CornerBasedShape) cornerModifier
            else genericModifier
        )
        .then(if (clip) Modifier.clip(resultShape) else Modifier)
        .then(if (resultPadding > 0.dp) Modifier.padding(resultPadding) else Modifier)
}


val LocalContainerShape = compositionLocalOf<Shape?> { null }

private val LocalContainerColor = compositionLocalOf<Color?> { null }

private val SafeLocalContainerColor
    @Composable
    get() = LocalContainerColor.current ?: MaterialTheme.colorScheme.surfaceContainerLow