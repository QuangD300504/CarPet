package com.example.vetbook.domain.models

enum class OrderStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Chờ xử lý"),
    PAID("PAID", "Đã thanh toán"),
    SHIPPED("SHIPPED", "Đang giao"),
    DELIVERED("DELIVERED", "Đã giao"),
    CANCELLED("CANCELLED", "Đã hủy");

    companion object {
        fun fromString(value: String): OrderStatus =
            entries.find { it.value == value } ?: PENDING
    }
}
