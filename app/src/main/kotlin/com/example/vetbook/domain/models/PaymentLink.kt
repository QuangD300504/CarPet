package com.example.vetbook.domain.models

import androidx.annotation.Keep

@Keep
data class PaymentLink(
    val checkoutUrl: String,
    val orderCode: Long,
    val paymentLinkId: String?
)