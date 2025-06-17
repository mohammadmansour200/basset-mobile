package com.basset.operations.presentation.cut_operation

import com.basset.operations.domain.cut_operation.CutOperationError

sealed interface CutOperationEvent {
    data class Error(val error: CutOperationError) : CutOperationEvent
}