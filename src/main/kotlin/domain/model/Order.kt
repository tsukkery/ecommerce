package com.ecommerce.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class Order(
    val id: UUID,
    val userId: UUID,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

@Serializable
data class OrderItem(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val quantity: Int,
    val price: BigDecimal,
    val createdAt: LocalDateTime
)