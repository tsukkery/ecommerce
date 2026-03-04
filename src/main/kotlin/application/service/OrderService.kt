package com.ecommerce.application.service

import com.ecommerce.application.port.OrderRepository
import com.ecommerce.application.port.ProductRepository
import com.ecommerce.application.port.MessageQueuePort
import com.ecommerce.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val messageQueuePort: MessageQueuePort
) {
    suspend fun createOrder(userId: UUID, items: List<OrderItemRequest>): Order {
        val orderId = UUID.randomUUID()
        val orderItems = mutableListOf<OrderItem>()
        var totalAmount = BigDecimal.ZERO

        for (item in items) {
            val product = productRepository.findById(item.productId)
                ?: throw IllegalArgumentException("Product ${item.productId} not found")

            if (product.stock < item.quantity) {
                throw IllegalArgumentException("Insufficient stock for product ${product.name}")
            }

            val orderItem = OrderItem(
                id = UUID.randomUUID(),
                orderId = orderId,
                productId = product.id,
                quantity = item.quantity,
                price = product.price,
                createdAt = LocalDateTime.now()
            )
            orderItems.add(orderItem)
            totalAmount += product.price * BigDecimal(item.quantity)

            // Update stock
            val updatedProduct = product.copy(
                stock = product.stock - item.quantity,
                updatedAt = LocalDateTime.now()
            )
            productRepository.update(updatedProduct)
        }

        val order = Order(
            id = orderId,
            userId = userId,
            status = OrderStatus.PENDING,
            totalAmount = totalAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val createdOrder = orderRepository.create(order, orderItems)

        // Publish event to queue
        messageQueuePort.publish(
            DomainEvent.OrderCreated(
                orderId = orderId.toString(),
                userId = userId.toString(),
                totalAmount = totalAmount.toDouble(),
                items = orderItems.map {
                    OrderItemEvent(
                        productId = it.productId.toString(),
                        quantity = it.quantity,
                        price = it.price.toDouble()
                    )
                }
            )
        )

        return createdOrder
    }

    suspend fun getUserOrders(userId: UUID): List<Order> {
        return orderRepository.findByUserId(userId)
    }

    suspend fun cancelOrder(orderId: UUID, userId: UUID, isAdmin: Boolean): Boolean {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("Order not found")

        if (order.userId != userId && !isAdmin) {
            throw IllegalArgumentException("Access denied")
        }

        if (order.status == OrderStatus.CANCELLED) {
            throw IllegalArgumentException("Order already cancelled")
        }

        if (order.status != OrderStatus.PENDING) {
            throw IllegalArgumentException("Only pending orders can be cancelled")
        }

        val cancelled = orderRepository.updateStatus(orderId, OrderStatus.CANCELLED)

        if (cancelled) {
            messageQueuePort.publish(
                DomainEvent.OrderCancelled(
                    orderId = orderId.toString(),
                    userId = userId.toString()
                )
            )
        }

        return cancelled
    }

    suspend fun getOrderStats(startDate: String?, endDate: String?): OrderStats {
        return orderRepository.getOrderStats(startDate, endDate)
    }
}

data class OrderItemRequest(
    val productId: UUID,
    val quantity: Int
)