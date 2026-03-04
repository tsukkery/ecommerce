package com.ecommerce.application.port

import com.ecommerce.domain.model.User
import java.util.*

interface UserRepository {
    suspend fun create(user: User): User
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun update(user: User): User
    suspend fun delete(id: UUID): Boolean
}