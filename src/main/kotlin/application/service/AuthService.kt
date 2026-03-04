package com.ecommerce.application.service

import com.ecommerce.application.port.UserRepository
import com.ecommerce.domain.model.User
import com.ecommerce.domain.model.UserRole
import at.favre.lib.crypto.bcrypt.BCrypt
import java.time.LocalDateTime
import java.util.*

class AuthService(
    private val userRepository: UserRepository
) {
    suspend fun register(email: String, password: String, firstName: String, lastName: String): User {
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with email $email already exists")
        }

        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        val user = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = hashedPassword,
            firstName = firstName,
            lastName = lastName,
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        return userRepository.create(user)
    }

    suspend fun login(email: String, password: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid email or password")

        val isValid = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash).verified
        if (!isValid) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return user
    }
}