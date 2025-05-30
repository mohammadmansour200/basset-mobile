package com.basset.operations.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.basset.R
import com.basset.core.navigation.OperationRoute
import com.basset.core.presentation.components.IconWithTooltip
import com.basset.home.presentation.ThemeState
import com.basset.home.presentation.components.OperationType
import com.basset.operations.presentation.components.FlexibleTopBar
import com.basset.operations.presentation.components.FlexibleTopBarDefaults
import com.basset.operations.presentation.components.MediaInfoCard
import com.basset.ui.theme.AppTheme
import com.basset.ui.theme.isDarkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationScreen(
    modifier: Modifier = Modifier,
    pickedFile: OperationRoute,
    themeState: ThemeState,
    onGoBack: () -> Unit = {}
) {
    val accentColor = if (isDarkMode(themeState.theme)) Color.White else Color.Black
    Scaffold(topBar = {
        FlexibleTopBar(
            colors = FlexibleTopBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        IconButton(onClick = { onGoBack() }) {
                            IconWithTooltip(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                text = stringResource(R.string.back_btn),
                                surfaceColor = accentColor
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val localizedTitle = when (pickedFile.operationType) {
                            OperationType.COMPRESS -> R.string.operation_compress
                            OperationType.CONVERT -> R.string.operation_convert
                            OperationType.BG_REMOVE -> R.string.operation_remove_background
                            OperationType.CUT -> R.string.operation_cut
                        }
                        Text(
                            text = stringResource(localizedTitle),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.size(48.dp))
                }
                MediaInfoCard(pickedFile = pickedFile)
            }

        }
    }) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        }
    }
}

@PreviewLightDark
@Composable
private fun OperationScreenPreview() {
    AppTheme(dynamicColor = false) {
        OperationScreen(
            pickedFile = OperationRoute(),
            themeState = ThemeState(),
        )
    }
}
