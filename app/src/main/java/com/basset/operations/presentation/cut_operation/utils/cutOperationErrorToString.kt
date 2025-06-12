package com.basset.operations.presentation.cut_operation.utils

import android.content.Context
import com.basset.R
import com.basset.operations.domain.cutOperation.CutOperationError

fun CutOperationError.toString(context: Context): String {
    val resId = when (this) {
        CutOperationError.MEDIA_PREVIEW_LOADING -> R.string.error_media_preview_loading
        CutOperationError.WRONG_START_CUT_RANGE_POSITION -> R.string.error_wrong_start_cut_range_position
        CutOperationError.WRONG_END_CUT_RANGE_POSITION -> R.string.error_wrong_end_cut_range_position
        CutOperationError.CUT_RANGE_EXCEEDS_DURATION -> R.string.error_cut_range_exceeds_duration
    }
    return context.getString(resId)
}