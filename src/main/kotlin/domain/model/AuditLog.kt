package com.ecommerce.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class AuditLog(
    val id: UUID,
    val userId: UUID?,
    val action: String,
    val entityType: String,
    val entityId: UUID?,
    val details: String,
    val ipAddress: String?,
    val createdAt: LocalDateTime
)