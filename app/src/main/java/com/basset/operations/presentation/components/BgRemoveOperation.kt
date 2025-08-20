package com.basset.operations.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.basset.R
import com.basset.core.presentation.modifier.ContainerShapeDefaults
import com.basset.core.presentation.modifier.container
import com.basset.operations.domain.model.Rate
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import kotlinx.coroutines.launch

@Composable
fun BgRemoveOperation(
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState,
    snackbarHostState: SnackbarHostState
) {
    val snackScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedOption by remember { mutableIntStateOf(0) }
    var selectedColor by remember { mutableStateOf(HsvColor.from(Color.White)) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBlurRate by remember { mutableStateOf<Rate>(Rate.LOW) }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {

        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.background_card_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )
        AnimatedContent(
            targetState = selectedOption,
            label = "bg_remove_operation_animation",
            transitionSpec = {
                fadeIn(animationSpec = tween(220)).togetherWith(
                    fadeOut(
                        animationSpec = tween(90)
                    )
                )
            }
        ) { option ->
            Column {
                val containerModifier = @Composable { i: Int ->
                    if (option != 0) Modifier.container(
                        shape = ContainerShapeDefaults.shapeForIndex(i, 2),
                        color = MaterialTheme.colorScheme.surface
                    ) else Modifier
                }
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .then(containerModifier(0))
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    val options = listOf(
                        Pair(stringResource(R.string.none_background_option), 0),
                        Pair(stringResource(R.string.color_background_option), 1),
                        Pair(stringResource(R.string.image_background_option), 2),
                        Pair(stringResource(R.string.blurry_background_option), 3)
                    )
                    options.forEach { (label, index) ->
                        EnhancedChip(
                            onClick = {
                                selectedOption = index
                            },
                            selected = index == option,
                            label = {
                                Text(text = label)
                            },
                            selectedColor = MaterialTheme.colorScheme.tertiary,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                if (option != 0) {
                    Spacer(Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .then(containerModifier(1))
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedOption == 1) {
                            Surface(
                                color = selectedColor.toColor(), modifier = Modifier
                                    .width(200.dp)
                                    .height(20.dp)
                            ) {}
                            Spacer(Modifier.height(4.dp))
                            ClassicColorPicker(
                                modifier = Modifier
                                    .size(200.dp),
                                color = selectedColor,
                                showAlphaBar = false,
                                onColorChanged = {
                                    selectedColor = it
                                })
                        }
                        if (selectedOption == 2) {
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent()
                            ) {
                                if (it == null) return@rememberLauncherForActivityResult

                                if (context.contentResolver.getType(it)
                                        ?.contains("gif") == true
                                ) snackScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.gif_not_supported_err))
                                } else selectedImageUri = it
                            }
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

                            selectedImageUri?.let { uriString ->
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = uriString,
                                    contentDescription = stringResource(R.string.selected_background_image_content_desc),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .height(120.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                        if (selectedOption == 3) {
                            FlowRow(
                                verticalArrangement = Arrangement.spacedBy(
                                    space = 8.dp,
                                    alignment = Alignment.CenterVertically
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 8.dp,
                                    alignment = Alignment.CenterHorizontally
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            ) {
                                val options = Rate.entries.toTypedArray()
                                options.forEach {
                                    val localizedOption = when (it) {
                                        Rate.LOW -> R.string.rate_option_low
                                        Rate.MEDIUM -> R.string.rate_option_medium
                                        Rate.HIGH -> R.string.rate_option_high
                                    }
                                    EnhancedChip(
                                        onClick = {
                                            selectedBlurRate = it
                                        },
                                        selected = selectedBlurRate == it,
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = stringResource(localizedOption))
                                            }
                                        },
                                        selectedColor = MaterialTheme.colorScheme.tertiary,
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    OutputSettings(
        onAction = { onAction(it) },
        operationScreenState = operationScreenState,
        isAudio = false
    )

    ExecuteOperationBtn(
        buttonLabel = stringResource(R.string.operation_remove_background_btn),
        onAction = {
            if (selectedOption == 2 && selectedImageUri == null) {
                snackScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.please_select_an_image_err))
                }
                return@ExecuteOperationBtn
            }

            val background = when (selectedOption) {
                1 -> selectedColor
                2 -> selectedImageUri
                3 -> selectedBlurRate
                else -> null
            }
            onAction(OperationScreenAction.OnRemoveBackground(background))
        },
        operationScreenState = operationScreenState
    )
}
