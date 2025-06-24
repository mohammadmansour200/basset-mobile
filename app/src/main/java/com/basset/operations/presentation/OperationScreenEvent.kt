package com.basset.operations.presentation

sealed interface OperationScreenEvent {
    object Error : OperationScreenEvent
    object Success : OperationScreenEvent
}