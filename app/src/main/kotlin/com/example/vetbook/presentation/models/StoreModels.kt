package com.example.vetbook.presentation.models

data class Product(
    val id: String,
    val name: String,
    val price: String,
    val imageUrl: String? = null,
    val description: String? = null
)

data class CartItem(
    val id: String,
    val shopName: String,
    val productName: String,
    val category: String,
    val quantity: Int,
    val price: String,
    val productId: String
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class OrderSummary(
    val itemCount: Int,
    val subtotal: Double,
    val discount: Double,
    val deliveryCharges: Double
) {
    val total: Double
        get() = subtotal - discount + deliveryCharges
}

