package com.basset.operations.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.basset.R
import com.basset.core.domain.model.MediaType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.domain.model.CompressionRate
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState
import kotlinx.coroutines.launch

@Composable
fun CompressOperation(
    modifier: Modifier = Modifier,
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState,
    snackbarHostState: SnackbarHostState,
    pickedFile: OperationRoute
) {
    val snackScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedCompressionRate by remember { mutableStateOf<CompressionRate?>(null) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.compress_card_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )
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
            val options = CompressionRate.entries.toTypedArray()
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Ltr,
            ) {
                options.forEach {
                    val localizedOption = when (it) {
                        CompressionRate.LOW -> R.string.quality_option_low
                        CompressionRate.MEDIUM -> R.string.quality_option_medium
                        CompressionRate.HIGH -> R.string.quality_option_high
                    }
                    EnhancedChip(
                        onClick = {
                            selectedCompressionRate = it
                        },
                        selected = selectedCompressionRate == it,
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = stringResource(localizedOption))
                            }
                        },
                        selectedColor = MaterialTheme.colorScheme.tertiary,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    ExecuteOperationBtn(
        onAction = {
            if (selectedCompressionRate == null) {
                snackScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.no_compress_selected_err))
                }
                return@ExecuteOperationBtn
            }
            onAction(
                OperationScreenAction.OnCompress(
                    selectedCompressionRate!!
                )
            )
        },
        isCancellable = pickedFile.mediaType != MediaType.IMAGE,
        operationScreenState = operationScreenState,
        buttonLabel = stringResource(R.string.operation_compress_btn)
    )
}
