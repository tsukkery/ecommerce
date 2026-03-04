package com.ecommerce.bootstrap

import com.ecommerce.application.port.UserRepository
import com.ecommerce.application.service.AuthService
import com.ecommerce.domain.model.User
import com.ecommerce.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val authService = AuthService(userRepository)

    @Test
    fun `should register new user`() = runBlocking {
        val email = "test@example.com"
        val password = "password123"
        val firstName = "John"
        val lastName = "Doe"

        coEvery { userRepository.findByEmail(email) } returns null
        coEvery { userRepository.create(any()) } answers { firstArg() }

        val user = authService.register(email, password, firstName, lastName)

        assertNotNull(user)
        assertEquals(email, user.email)
        assertEquals(firstName, user.firstName)
        assertEquals(lastName, user.lastName)
        assertEquals(UserRole.USER, user.role)
        assertTrue(user.passwordHash.startsWith("$2a$")) // BCrypt hash format

        coVerify(exactly = 1) { userRepository.findByEmail(email) }
        coVerify(exactly = 1) { userRepository.create(any()) }
    }

    @Test
    fun `should throw exception when registering with existing email`() = runBlocking {
        val email = "existing@example.com"
        val existingUser = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = "hash",
            firstName = "Existing",
            lastName = "User",
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        coEvery { userRepository.findByEmail(email) } returns existingUser

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking {
                authService.register(email, "password", "John", "Doe")
            }
        }

        assertEquals("User with email $email already exists", exception.message)
    }
}