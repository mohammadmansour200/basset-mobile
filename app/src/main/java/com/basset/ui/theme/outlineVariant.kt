package com.basset.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

fun ColorScheme.outlineVariant(
    luminance: Float = 0.3f,
    onTopOf: Color = surfaceContainer
) = onSecondaryContainer
    .copy(alpha = luminance)
    .compositeOver(onTopOf)