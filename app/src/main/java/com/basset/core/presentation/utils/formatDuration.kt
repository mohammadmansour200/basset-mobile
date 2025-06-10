package com.basset.core.presentation.utils


fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0)
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%d:%02d", minutes, seconds)
}

fun Double.formatDuration(): String {
    val totalMillis = this.toLong()
    val totalSeconds = totalMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val millis = totalMillis % 1000

    return if (hours > 0)
        String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    else
        String.format("%02d:%02d.%03d", minutes, seconds, millis)
}

fun String.parseDuration(): Double {
    val parts = this.split(":", ".", limit = 4)
    // Check if milliseconds are present
    val hasMillis = this.contains(".")

    return when {
        parts.size == 2 && !hasMillis -> { // mm:ss
            val minutes = parts[0].toLong()
            val seconds = parts[1].toLong()
            (minutes * 60_000 + seconds * 1000).toDouble()
        }

        parts.size == 3 && hasMillis -> { // mm:ss.mmm
            val minutes = parts[0].toLong()
            val seconds = parts[1].toLong()
            val millis = parts[2].padEnd(3, '0').take(3).toLong()
            (minutes * 60_000 + seconds * 1000 + millis).toDouble()
        }

        parts.size == 3 && !hasMillis -> { // hh:mm:ss
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            val seconds = parts[2].toLong()
            (hours * 3600_000 + minutes * 60_000 + seconds * 1000).toDouble()
        }

        parts.size == 4 && hasMillis -> { // hh:mm:ss.mmm
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            val seconds = parts[2].toLong()
            val millis = parts[3].padEnd(3, '0').take(3).toLong()
            (hours * 3600_000 + minutes * 60_000 + seconds * 1000 + millis).toDouble()
        }

        else -> throw IllegalArgumentException("Invalid duration format: $this")
    }
}

fun String.isValidDurationFormat(): Boolean {
    val durationRegex = Regex("""^((\d{1,2}:)?\d{2}:\d{2}(\.\d{1,3})?|\d{1,2}:\d{2})$""")
    return this.matches(durationRegex)
}

