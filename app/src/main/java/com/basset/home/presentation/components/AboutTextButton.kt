package com.basset.home.presentation.components

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.net.toUri

@Composable
fun AboutTextButton(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes descriptionId: Int,
    url: String,
    @DrawableRes iconId: Int
) {
    val context = LocalContext.current
    val intent = remember { Intent(Intent.ACTION_VIEW, url.toUri()) }

    TextButton(
        modifier = modifier,
        onClick = {
            context.startActivity(intent)
        },
        shape = MaterialTheme.shapes.extraSmall,
        contentPadding = PaddingValues()
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(titleId)) },
            supportingContent = {
                Text(
                    text = stringResource(descriptionId),
                    modifier = Modifier.alpha(0.8f)
                )
            },
            leadingContent = {
                Icon(
                    ImageVector.vectorResource(iconId),
                    null,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}