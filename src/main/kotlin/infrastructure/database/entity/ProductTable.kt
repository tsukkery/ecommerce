package com.ecommerce.infrastructure.database.entity

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.math.BigDecimal

object ProductTable : UUIDTable("products") {
    val name = varchar("name", 255)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val stock = integer("stock")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    init {
        index(false, name)
    }
}