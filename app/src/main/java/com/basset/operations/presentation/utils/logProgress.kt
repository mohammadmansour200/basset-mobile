package com.basset.operations.presentation.utils

import com.arthenica.ffmpegkit.Log
import com.basset.core.presentation.utils.parseDuration

fun Log.progress(duration: Long): Float {
    val timeRegex = "time=(\\d{2}:\\d{2}:\\d{2}\\.\\d{2})".toRegex()
    val match = timeRegex.find(message)

    if (match == null || match.groupValues.size < 2) return 0f

    val currentEncodingProgress = match.groupValues[1].parseDuration()
    val currentEncodingProgressSeconds = currentEncodingProgress / 1000
    val progressPercentage = currentEncodingProgressSeconds / duration
    android.util.Log.d(
        "ffmpeg-kit",
        "Encoding Progress: $currentEncodingProgressSeconds, Duration: $duration "
    )
    return progressPercentage.toFloat()
}
