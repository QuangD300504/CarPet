package com.example.vetbook.domain.models

import androidx.annotation.Keep

@Keep
data class StoreProduct(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val description: String? = null,
    val category: String? = null,
    val stock: Int = 0,
    val shopName: String = "VetBook Shop",
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

