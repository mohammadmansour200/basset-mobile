package com.basset.operations.domain.cut_operation

enum class CutOperationError {
    MEDIA_PREVIEW_LOADING,
    WRONG_START_CUT_RANGE_POSITION,
    WRONG_END_CUT_RANGE_POSITION,
    CUT_RANGE_EXCEEDS_DURATION
}