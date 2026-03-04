package com.ecommerce.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class Product(
    val id: UUID,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)