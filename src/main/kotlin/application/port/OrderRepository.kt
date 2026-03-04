package com.ecommerce.application.port

import com.ecommerce.domain.model.Order
import com.ecommerce.domain.model.OrderItem
import com.ecommerce.domain.model.OrderStatus
import java.util.*

interface OrderRepository {
    suspend fun create(order: Order, items: List<OrderItem>): Order
    suspend fun findById(id: UUID): Order?
    suspend fun findByUserId(userId: UUID): List<Order>
    suspend fun findAllOrders(limit: Int, offset: Int): List<Order>
    suspend fun updateStatus(id: UUID, status: OrderStatus): Boolean
    suspend fun getOrderStats(startDate: String?, endDate: String?): OrderStats
}

data class OrderStats(
    val totalOrders: Long,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val ordersByStatus: Map<String, Long>
)