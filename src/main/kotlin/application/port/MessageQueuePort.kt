package com.ecommerce.application.port

import kotlinx.serialization.Serializable

interface MessageQueuePort {
    suspend fun publish(event: DomainEvent)
    suspend fun subscribe(handler: (DomainEvent) -> Unit)
}

@Serializable
sealed class DomainEvent {
    @Serializable
    data class OrderCreated(
        val orderId: String,
        val userId: String,
        val totalAmount: Double,
        val items: List<OrderItemEvent>
    ) : DomainEvent()

    @Serializable
    data class OrderCancelled(
        val orderId: String,
        val userId: String
    ) : DomainEvent()

    @Serializable
    data class ProductStockUpdated(
        val productId: String,
        val newStock: Int
    ) : DomainEvent()
}

@Serializable
data class OrderItemEvent(
    val productId: String,
    val quantity: Int,
    val price: Double
)