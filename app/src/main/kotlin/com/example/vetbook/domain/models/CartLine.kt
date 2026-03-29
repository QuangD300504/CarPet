package com.example.vetbook.domain.models

import androidx.annotation.Keep

@Keep
data class CartLine(
    val productId: String,
    val quantity: Int,
    val addedAt: Long? = null
)

