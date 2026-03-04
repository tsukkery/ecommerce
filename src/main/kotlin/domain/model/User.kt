package com.ecommerce.domain.model

import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class UserRole {
    USER,
    ADMIN
}