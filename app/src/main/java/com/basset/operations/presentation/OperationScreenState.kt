package com.basset.operations.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class OperationScreenState(
    val isOperating: Boolean = false,
    val progress: Float? = null,
)
