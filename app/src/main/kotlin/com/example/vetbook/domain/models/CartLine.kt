package com.example.vetbook.domain.models

data class CartLine(
    val productId: String,
    val quantity: Int,
    val addedAt: Long? = null
)

