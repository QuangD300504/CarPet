package com.example.vetbook.data.models

data class StoreProductDto(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val description: String? = null,
    val category: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

data class CartLineDto(
    val productId: String = "",
    val quantity: Int = 0,
    val addedAt: Long? = null
)

