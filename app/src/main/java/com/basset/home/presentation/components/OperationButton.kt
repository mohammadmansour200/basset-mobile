package com.basset.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.basset.R

enum class MimeType {
    AUDIO, IMAGE, VIDEO
}

enum class OperationType {
    COMPRESS, CONVERT, BG_REMOVE, CUT
}

data class Operation(val text: String, val icon: Int, val operationType: OperationType)

@Composable
fun OperationsButton(actions: List<Operation>, mimeType: MimeType) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.operation_alert_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.size(5.dp))
        actions.forEach { action ->
            TextButton(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(23.dp)),
                shape = MaterialTheme.shapes.extraSmall,
                contentPadding = PaddingValues()
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            ImageVector.vectorResource(action.icon),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(action.text) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
