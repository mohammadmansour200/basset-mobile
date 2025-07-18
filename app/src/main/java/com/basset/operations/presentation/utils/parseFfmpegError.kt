package com.basset.operations.presentation.utils

import com.basset.operations.domain.model.OperationError

fun parseFfmpegError(logs: String): OperationError {
    val contains = { label: String -> logs.contains(label, ignoreCase = true) }

    if (contains("No such file or directory")) OperationError.ERROR_READING_INPUT

    if (contains("Could not open file")) OperationError.ERROR_READING_INPUT

    if (contains("Invalid data found when processing input")) OperationError.ERROR_INVALID_FORMAT

    if (contains("No space left on device")) OperationError.ERROR_WRITING_OUTPUT

    if (contains("Output file #0 does not contain any stream")) OperationError.ERROR_WRITING_OUTPUT

    return OperationError.ERROR_UNKNOWN
}