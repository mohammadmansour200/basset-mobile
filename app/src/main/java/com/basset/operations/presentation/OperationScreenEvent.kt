package com.basset.operations.presentation

import kotlinx.coroutines.CompletableDeferred

sealed interface OperationScreenEvent {
    object Error : OperationScreenEvent
    object Success : OperationScreenEvent
    class PermissionRequired(
        val deferred: CompletableDeferred<Boolean>
    ) : OperationScreenEvent
}