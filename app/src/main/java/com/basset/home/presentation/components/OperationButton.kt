package com.basset.home.presentation.components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.basset.R
import com.basset.core.navigation.OperationRoute

enum class MimeType {
    AUDIO, IMAGE, VIDEO
}

enum class OperationType {
    COMPRESS, CONVERT, BG_REMOVE, CUT
}

data class OperationButtonData(val text: String, val icon: Int, val operationType: OperationType)

@Composable
fun OperationsButtons(uri: Uri, onGoToOperation: (OperationRoute) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(start = 10.dp, top = 15.dp, end = 10.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.operation_alert_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.size(15.dp))

        val textButtonModifier = Modifier
            .fillMaxWidth()

        val type = context.contentResolver.getType(uri)

        val mimeType = when {
            type!!.contains("video", ignoreCase = true) -> MimeType.VIDEO
            type.contains("audio", ignoreCase = true) -> MimeType.AUDIO
            else -> MimeType.IMAGE
        }

        val actions = when (mimeType) {
            MimeType.IMAGE -> {
                // Handle image files
                listOf(
                    OperationButtonData(
                        stringResource(R.string.operation_compress),
                        R.drawable.compress,
                        operationType = OperationType.COMPRESS
                    ),
                    OperationButtonData(
                        stringResource(R.string.operation_convert),
                        R.drawable.convert_image,
                        operationType = OperationType.CONVERT
                    ),
                    OperationButtonData(
                        stringResource(R.string.operation_remove_background),
                        R.drawable.background_remove,
                        operationType = OperationType.BG_REMOVE
                    )
                )
            }

            MimeType.AUDIO -> {
                // Handle audio files
                listOf(
                    OperationButtonData(
                        stringResource(R.string.operation_trim),
                        R.drawable.cut,
                        operationType = OperationType.CUT
                    ),
                    OperationButtonData(
                        stringResource(R.string.operation_compress),
                        R.drawable.compress,
                        operationType = OperationType.COMPRESS
                    ),
                    OperationButtonData(
                        stringResource(R.string.operation_convert),
                        R.drawable.convert_audio,
                        operationType = OperationType.CONVERT
                    )
                )
            }

            MimeType.VIDEO -> {
                // Handle video files
                listOf(
                    OperationButtonData(
                        stringResource(R.string.operation_trim),
                        R.drawable.cut,
                        operationType = OperationType.CUT
                    ),
                    OperationButtonData(
                        stringResource(R.string.operation_compress),
                        R.drawable.compress,
                        operationType = OperationType.COMPRESS
                    ),
                    OperationButtonData(
                        stringResource(R.string.operation_convert),
                        R.drawable.convert_video,
                        operationType = OperationType.CONVERT
                    )
                )
            }
        }


        actions.forEachIndexed { i, action ->
            TextButton(
                onClick = {
                    onGoToOperation(
                        OperationRoute(
                            mimeType = mimeType,
                            uri = uri.toString(),
                            operationType = action.operationType
                        )
                    )
                },
                modifier =
                    when (i) {
                        0 -> textButtonModifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                )
                            )

                        actions.size - 1 -> textButtonModifier
                            .clip(
                                RoundedCornerShape(
                                    bottomEnd = 16.dp,
                                    bottomStart = 16.dp
                                )
                            )

                        else -> textButtonModifier
                            .clip(
                                RoundedCornerShape(
                                    0.dp
                                )
                            )
                    },
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
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}
