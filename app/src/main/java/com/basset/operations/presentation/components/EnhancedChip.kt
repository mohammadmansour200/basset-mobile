package com.basset.operations.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.basset.core.presentation.modifier.LocalContainerShape
import com.basset.core.presentation.modifier.container
import com.basset.ui.theme.outlineVariant

@Composable
fun EnhancedChip(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(6.dp),
    selectedColor: Color,
    selectedContentColor: Color = MaterialTheme.colorScheme.contentColorFor(selectedColor),
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(10.dp),
    interactionSource: MutableInteractionSource? = null,
    defaultMinSize: Dp = 36.dp,
    label: @Composable () -> Unit
) {
    val color by animateColorAsState(
        if (selected) selectedColor
        else unselectedColor
    )
    val contentColor by animateColorAsState(
        if (selected) selectedContentColor
        else unselectedContentColor
    )

    val realInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        ),
        LocalContentColor provides contentColor,
        LocalContainerShape provides null
    ) {
        Box(
            modifier = modifier
                .defaultMinSize(defaultMinSize, defaultMinSize)
                .container(
                    color = color,
                    resultPadding = 0.dp,
                    borderColor = if (!selected) MaterialTheme.colorScheme.outlineVariant()
                    else selectedColor
                        .copy(alpha = 0.9f)
                        .compositeOver(Color.Black),
                    shape = shape,
                    autoShadowElevation = 0.5.dp
                )
                .then(
                    onClick?.let {
                        Modifier.clickable(
                            indication = LocalIndication.current,
                            interactionSource = realInteractionSource,
                            onClick = onClick
                        )
                    } ?: Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                label()
            }
        }
    }
}
