package com.example.vetbook.domain.models

data class StoreOrder(
    val id: String,
    val uid: String,
    val orderCode: String,
    val items: List<OrderItem>,
    val itemCount: Int,
    val subtotal: Double,
    val discount: Double,
    val deliveryCharges: Double,
    val total: Double,
    val status: OrderStatus,
    val createdAt: Long
)

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val lineTotal: Double
)
