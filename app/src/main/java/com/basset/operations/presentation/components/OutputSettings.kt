package com.basset.operations.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.basset.R
import com.basset.core.presentation.modifier.ContainerShapeDefaults
import com.basset.core.presentation.modifier.container
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState
import kotlinx.coroutines.launch

@Composable
fun OutputSettings(
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState,
    snackbarHostState: SnackbarHostState? = null,
    isAudio: Boolean
) {
    val context = LocalContext.current
    val snackScope = rememberCoroutineScope()

    Spacer(Modifier.padding(top = 10.dp))

    var expanded by remember { mutableStateOf(false) }
    val degrees by animateFloatAsState(if (expanded) -90f else 90f)
    Row(
        modifier = Modifier
            .clickable { expanded = expanded.not() },
    ) {
        Text(
            stringResource(R.string.output_settings_title)
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.chevron_right),
            contentDescription = null,
            modifier = Modifier.rotate(degrees),
        )
    }
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            )
        ),
        exit = shrinkVertically()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Spacer(Modifier.height(8.dp))
            val settingsSize = if (isAudio) 3 else 1
            Column {
                val containerModifier = @Composable { i: Int ->
                    if (isAudio) Modifier.container(
                        shape = ContainerShapeDefaults.shapeForIndex(i, settingsSize),
                        color = MaterialTheme.colorScheme.surface
                    ) else Modifier
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .then(containerModifier(0))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isAudio)
                        OutlinedTextField(
                            label = { Text(stringResource(R.string.output_settings_media_title)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.title),
                                    contentDescription = null,
                                )
                            },
                            value = operationScreenState.outputFilename ?: "",
                            onValueChange = { newText ->
                                onAction(OperationScreenAction.OnSetOutputFilename(newText))
                            },
                        )
                    else TextField(
                        label = { Text(stringResource(R.string.output_settings_media_title)) },
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.title),
                                contentDescription = null,
                            )
                        },
                        value = operationScreenState.outputFilename ?: "",
                        onValueChange = { newText ->
                            onAction(OperationScreenAction.OnSetOutputFilename(newText))
                        },
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (isAudio) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .then(containerModifier(1))
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            label = { Text(stringResource(R.string.output_settings_media_author)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.artist),
                                    contentDescription = null,
                                )
                            },
                            value = operationScreenState.outputAuthor ?: "",
                            onValueChange = { newText ->
                                onAction(OperationScreenAction.OnSetOutputAuthor(newText))
                            },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .then(containerModifier(2))
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) {
                            if (it == null) return@rememberLauncherForActivityResult

                            if (context.contentResolver.getType(it)
                                    ?.contains("gif") == true
                            ) snackScope.launch {
                                snackbarHostState?.showSnackbar(context.getString(R.string.gif_not_supported_err))
                            } else onAction(OperationScreenAction.OnSetOutputAlbumArt(it))
                        }
                        Text(
                            text = stringResource(R.string.output_settings_media_album),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Button(modifier = Modifier.width(200.dp), onClick = {
                            launcher.launch("image/*")
                        }) {
                            Text(
                                text = if (operationScreenState.outputAlbumArt == null) stringResource(
                                    R.string.choose_image_label
                                ) else stringResource(
                                    R.string.change_image_label
                                )
                            )
                        }

                        operationScreenState.outputAlbumArt?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = uri,
                                contentDescription = stringResource(R.string.selected_background_image_content_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(120.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}