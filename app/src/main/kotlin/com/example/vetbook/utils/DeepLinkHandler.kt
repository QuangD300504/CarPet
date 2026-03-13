package com.example.vetbook.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkHandler {
    private val _paymentResult = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val paymentResult = _paymentResult.asSharedFlow()

    fun emitPaymentResult(isSuccess: Boolean) {
        _paymentResult.tryEmit(isSuccess)
    }

    fun clear() {
        _paymentResult.resetReplayCache()
    }
}
