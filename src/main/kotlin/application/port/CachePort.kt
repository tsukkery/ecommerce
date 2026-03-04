package com.ecommerce.application.port

import kotlinx.serialization.Serializable

interface CachePort {
    suspend fun <T : Any> set(key: String, value: T, ttlSeconds: Long = 300)
    suspend fun <T : Any> get(key: String, clazz: Class<T>): T?
    suspend fun delete(key: String)
    suspend fun exists(key: String): Boolean
}