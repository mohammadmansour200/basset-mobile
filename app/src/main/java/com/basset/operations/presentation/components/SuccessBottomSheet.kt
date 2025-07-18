package com.basset.operations.presentation.components

import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.basset.R
import com.basset.core.domain.model.OperationType
import com.basset.core.utils.MimeTypeMap
import com.basset.operations.domain.model.Metadata

@Composable
fun SuccessBottomSheet(
    modifier: Modifier = Modifier,
    outputedFile: Uri?,
    inputFileMetadata: Metadata,
    outputedFileMetadata: Metadata,
    operationType: OperationType
) {
    val context = LocalContext.current

    val formattedOutputedFileSize = Formatter.formatFileSize(
        context,
        outputedFileMetadata.fileSizeBytes ?: 0
    )
    val formattedInputFileSize = Formatter.formatFileSize(
        context,
        inputFileMetadata.fileSizeBytes ?: 0
    )

    Column {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.success_filled),
                    contentDescription = null,
                    tint = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.operation_success),
                    style = MaterialTheme.typography.titleLarge
                )
                if (operationType == OperationType.COMPRESS || operationType == OperationType.CONVERT) {
                    val text = when (operationType) {
                        OperationType.COMPRESS -> stringResource(
                            R.string.operation_success_compressed,
                            formattedInputFileSize,
                            formattedOutputedFileSize
                        )

                        else -> stringResource(
                            R.string.operation_success_converted,
                            inputFileMetadata.ext ?: "",
                            outputedFileMetadata.ext ?: ""
                        )
                    }

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Button(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                outputedFile?.let {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, it)
                        type = MimeTypeMap.getMimeTypeFromExtension(outputedFileMetadata.ext ?: "")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
            }
        ) {
            Text(text = stringResource(R.string.share_file))
        }
    }
}