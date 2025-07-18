package com.basset.operations.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.basset.R
import com.basset.operations.domain.model.OperationError

@Composable
fun ErrorBottomSheet(
    modifier: Modifier = Modifier,
    detailedErrorMessage: String?,
    operationError: OperationError?
) {
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
                painter = painterResource(id = R.drawable.close_filled),
                contentDescription = null,
                tint = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))

            val titleText = when (operationError) {
                OperationError.ERROR_READING_INPUT -> R.string.operation_error_input_file_title
                OperationError.ERROR_WRITING_OUTPUT -> R.string.operation_error_output_file_title
                OperationError.ERROR_INVALID_FORMAT -> R.string.operation_error_invalid_format_title
                else -> R.string.operation_error_unknown_title
            }
            Text(
                text = stringResource(titleText),
                style = MaterialTheme.typography.titleLarge
            )

            val descText = when (operationError) {
                OperationError.ERROR_READING_INPUT -> R.string.operation_error_input_file_desc
                OperationError.ERROR_WRITING_OUTPUT -> R.string.operation_error_output_file_desc
                else -> null
            }
            descText?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            detailedErrorMessage?.let {
                var expanded by remember { mutableStateOf(false) }
                val degrees by animateFloatAsState(if (expanded) -90f else 90f)
                Row(
                    modifier = Modifier
                        .clickable { expanded = expanded.not() },
                ) {
                    Text(
                        stringResource(R.string.operation_error_detailed_desc)
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
                    Box(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}