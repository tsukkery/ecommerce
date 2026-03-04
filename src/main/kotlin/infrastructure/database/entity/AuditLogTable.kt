package com.ecommerce.infrastructure.database.entity

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object AuditLogTable : UUIDTable("audit_logs") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val action = varchar("action", 100)
    val entityType = varchar("entity_type", 100)
    val entityId = uuid("entity_id").nullable()
    val details = text("details")
    val ipAddress = varchar("ip_address", 45).nullable()
    val createdAt = datetime("created_at")

    init {
        index(false, userId)
        index(false, action)
        index(false, entityType)
        index(false, createdAt)
    }
}