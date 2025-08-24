package com.basset.home.presentation

import android.net.Uri
import android.view.Gravity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basset.R
import com.basset.core.domain.CoreConstants.AUDIO_MIME_TYPES
import com.basset.core.domain.CoreConstants.IMAGE_MIME_TYPES
import com.basset.core.domain.CoreConstants.PDF_MIME_TYPES
import com.basset.core.domain.CoreConstants.VIDEO_MIME_TYPES
import com.basset.core.navigation.OperationRoute
import com.basset.core.presentation.components.IconWithTooltip
import com.basset.home.presentation.components.BottomSheetType
import com.basset.home.presentation.components.HintCard
import com.basset.home.presentation.components.OperationsButtons
import com.basset.home.presentation.components.SheetContent
import com.basset.ui.theme.AppTheme
import com.basset.ui.theme.isDarkMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ThemeViewModel = koinViewModel(),
    receivedUri: Uri? = null,
    onGoToOperation: (OperationRoute) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var currentBottomSheet: BottomSheetType? by remember { mutableStateOf(null) }
    var pickedFile by remember { mutableStateOf(receivedUri) }

    val accentColor = if (isDarkMode(state.theme)) Color.White else Color.Black

    Scaffold(bottomBar = {
        BottomAppBar(
            actions = {
                var menuExpanded by remember { mutableStateOf(false) }

                val onMenuItemClick: (BottomSheetType) -> Unit = { type ->
                    currentBottomSheet = type
                    menuExpanded = false
                }

                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    IconWithTooltip(
                        icon = Icons.Filled.MoreVert,
                        text = stringResource(R.string.more_options),
                    )

                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_menu_item)) },
                        onClick = { onMenuItemClick(BottomSheetType.SETTINGS) },
                        leadingIcon = { Icon(Icons.Filled.Settings, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.about_menu_item)) },
                        onClick = { onMenuItemClick(BottomSheetType.ABOUT) },
                        leadingIcon = { Icon(Icons.Filled.Info, null) }
                    )
                }
            },

            floatingActionButton = {
                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                        if (it != null) {
                            pickedFile = it
                        }
                    }
                FloatingActionButton(
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                *IMAGE_MIME_TYPES,
                                *AUDIO_MIME_TYPES,
                                *VIDEO_MIME_TYPES,
                                *PDF_MIME_TYPES
                            )
                        )
                    }
                ) {
                    IconWithTooltip(
                        icon = ImageVector.vectorResource(R.drawable.folder_open),
                        text = stringResource(R.string.file_upload_fab),
                        iconModifier = Modifier.size(28.dp),
                    )
                }
            }
        )
    }) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.logo),
                contentDescription = stringResource(R.string.app_logo),
                tint = accentColor,
                modifier = Modifier.size(200.dp)
            )
            HintCard()
        }

        pickedFile?.let {
            Dialog(onDismissRequest = { pickedFile = null }) {
                val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
                dialogWindowProvider.window.setGravity(Gravity.BOTTOM)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    OperationsButtons(it, onGoToOperation = { it ->
                        pickedFile = null
                        onGoToOperation(it)
                    })
                }
            }
        }
    }

    if (currentBottomSheet != null) {
        ModalBottomSheet(
            onDismissRequest = {
                currentBottomSheet = null
            },
        ) {
            currentBottomSheet?.let { type ->
                SheetContent(
                    bottomSheetType = type,
                    settingsState = state,
                    settingsOnAction = { viewModel.onAction(it) }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    AppTheme(dynamicColor = false) {
        HomeScreen()
    }
}