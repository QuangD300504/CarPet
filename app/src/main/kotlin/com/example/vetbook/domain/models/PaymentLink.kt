package com.example.vetbook.domain.models

data class PaymentLink(
    val checkoutUrl: String,
    val orderCode: Long,
    val paymentLinkId: String?
)