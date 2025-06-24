package com.basset.operations.domain.model

enum class OperationError {
    ERROR_UNKNOWN,          // An unspecified error occurred
    ERROR_FILE_NOT_FOUND,   // Input file could not be found or accessed
    ERROR_READING_INPUT,    // Problem reading the input file
    ERROR_WRITING_OUTPUT,   // Problem writing the output file
    ERROR_DISK_SPACE,       // Insufficient disk space
    ERROR_FFMPEG_FAILED,    // FFmpeg process itself reported a failure (check FFmpeg logs)
    ERROR_INVALID_FORMAT,   // The format is not supported or invalid
    ERROR_BACKGROUND_REMOVAL, // Error during background removal process
    ERROR_IMAGE_PROCESSING, // General error during image ops
}