package com.basset.operations.presentation.cut_operation

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class CutOperationState(
    val amplitudes: List<Int> = emptyList(),
    val videoFrames: List<Bitmap> = emptyList(),
    val position: Long = 0L,
    val startRange: Float = 0f,
    val endRange: Float = 1f
)