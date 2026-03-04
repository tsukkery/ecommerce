package com.ecommerce.infrastructure.repository

import com.ecommerce.application.port.UserRepository
import com.ecommerce.domain.model.User
import com.ecommerce.domain.model.UserRole
import com.ecommerce.infrastructure.database.entity.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class UserRepositoryImpl : UserRepository {
    override suspend fun create(user: User): User = newSuspendedTransaction {
        UserTable.insert {
            it[id] = user.id
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[role] = user.role
            it[createdAt] = user.createdAt
            it[updatedAt] = user.updatedAt
        }
        user
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UserTable.select { UserTable.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UserTable.select { UserTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun update(user: User): User = newSuspendedTransaction {
        UserTable.update({ UserTable.id eq user.id }) {
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[role] = user.role
            it[updatedAt] = user.updatedAt
        }
        user
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        UserTable.deleteWhere { UserTable.id eq id } > 0
    }

    private fun ResultRow.toUser(): User = User(
        id = this[UserTable.id].value,
        email = this[UserTable.email],
        passwordHash = this[UserTable.passwordHash],
        firstName = this[UserTable.firstName],
        lastName = this[UserTable.lastName],
        role = this[UserTable.role],
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt]
    )
}