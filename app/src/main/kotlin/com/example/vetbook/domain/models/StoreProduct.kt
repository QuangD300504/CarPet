package com.example.vetbook.domain.models

data class StoreProduct(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val description: String? = null,
    val category: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

