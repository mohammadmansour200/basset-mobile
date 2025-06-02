package com.basset.operations.presentation

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class CutOperationMediaPlayerState(
    val amplitudes: List<Int> = emptyList(),
    val videoFrames: List<Bitmap> = emptyList(),
    val position: Long = 0L
)