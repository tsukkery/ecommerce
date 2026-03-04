package com.ecommerce.infrastructure.database.entity

import com.ecommerce.domain.model.OrderStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.math.BigDecimal

object OrderTable : UUIDTable("orders") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 50, OrderStatus::class)
    val totalAmount = decimal("total_amount", 10, 2)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    init {
        index(false, userId)
        index(false, status)
    }
}

object OrderItemTable : UUIDTable("order_items") {
    val orderId = reference("order_id", OrderTable, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", ProductTable, onDelete = ReferenceOption.RESTRICT)
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)
    val createdAt = datetime("created_at")

    init {
        index(false, orderId)
        index(false, productId)
    }
}