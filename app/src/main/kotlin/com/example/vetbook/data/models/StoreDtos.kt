package com.example.vetbook.data.models

data class StoreProductDto(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val description: String? = null,
    val category: String? = null,
    val stock: Int = 0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

data class CartLineDto(
    val productId: String = "",
    val quantity: Int = 0,
    val addedAt: Long? = null
)

data class StoreOrderDto(
    val id: String = "",
    val uid: String = "",
    val orderCode: String = "",
    val items: List<OrderItemDto> = emptyList(),
    val itemCount: Int = 0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val deliveryCharges: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "PENDING",
    val createdAt: Long = 0L,
    val checkoutUrl: String? = null,
    val receiverName: String? = null,
    val receiverPhone: String? = null,
    val deliveryAddress: String? = null
)

data class OrderItemDto(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val lineTotal: Double = 0.0,
    val imageUrl: String? = null
)