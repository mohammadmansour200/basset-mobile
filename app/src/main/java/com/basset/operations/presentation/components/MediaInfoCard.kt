package com.basset.operations.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.OpenableColumns
import android.text.format.Formatter
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.basset.R
import com.basset.core.navigation.OperationRoute
import com.basset.core.utils.formatDuration
import com.basset.core.utils.getFileName
import com.basset.home.presentation.components.MimeType

data class MediaMetadata(
    val title: String?,
    val artist: String?,
    val durationMs: Long?,
    val embeddedPicture: Bitmap?,
    val fileSizeBytes: Long?
)

@Composable
fun MediaInfoCard(
    modifier: Modifier = Modifier,
    pickedFile: OperationRoute
) {
    val context = LocalContext.current
    val uri = pickedFile.uri.toUri()

    val retriever = remember { MediaMetadataRetriever() }
    DisposableEffect(Unit) {
        onDispose {
            retriever.release()
        }
    }

    val metadata = remember(uri) {
        runCatching {
            val fileSizeBytes = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
                }

            if (pickedFile.mimeType == MimeType.IMAGE) {
                return@runCatching MediaMetadata(
                    title = uri.getFileName(context),
                    fileSizeBytes = fileSizeBytes,
                    artist = null,
                    durationMs = null,
                    embeddedPicture = null,
                )
            }

            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val embeddedPicture = retriever.embeddedPicture?.let {
                BitmapFactory.decodeByteArray(
                    it, 0,
                    it.size
                )
            }
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()

            MediaMetadata(
                title = title,
                artist = artist,
                durationMs = durationMs,
                embeddedPicture = embeddedPicture,
                fileSizeBytes = fileSizeBytes,
            )
        }.getOrNull()
    }

    val durationFormatted = metadata?.durationMs?.formatDuration() ?: "--:--"

    val image = when (pickedFile.mimeType) {
        MimeType.AUDIO -> metadata?.embeddedPicture
        MimeType.IMAGE -> context.contentResolver.openInputStream(uri).use { data ->
            BitmapFactory.decodeStream(data)
        }

        MimeType.VIDEO -> retriever.getFrameAtTime(0)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .align(Alignment.Center)
                .then(Modifier.widthIn(max = 500.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(targetState = image) { img ->
                    if (img != null) {
                        Image(
                            bitmap = img.asImageBitmap(),
                            contentDescription = stringResource(R.string.preview_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .aspectRatio(0.9f)
                        )
                    } else {
                        val icon = when (pickedFile.mimeType) {
                            MimeType.AUDIO -> R.drawable.music_note
                            MimeType.IMAGE -> R.drawable.image
                            MimeType.VIDEO -> R.drawable.movie
                        }
                        Icon(
                            imageVector = ImageVector.vectorResource(icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(68.dp)
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .aspectRatio(0.9f)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.2f), // light background hint
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = metadata?.title ?: uri.getFileName(context),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (pickedFile.mimeType == MimeType.AUDIO) {
                        Text(
                            text = metadata?.artist ?: stringResource(R.string.unknown_author),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${
                            Formatter.formatFileSize(
                                context,
                                metadata?.fileSizeBytes ?: 0
                            )
                        } | ${context.contentResolver.getType(uri)?.substringAfter("/")}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (pickedFile.mimeType != MimeType.IMAGE) {
                    Text(
                        text = durationFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}

