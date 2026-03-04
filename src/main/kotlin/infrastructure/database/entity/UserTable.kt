package com.ecommerce.infrastructure.database.entity

import com.ecommerce.domain.model.UserRole
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UserTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val role = enumerationByName("role", 50, UserRole::class)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}