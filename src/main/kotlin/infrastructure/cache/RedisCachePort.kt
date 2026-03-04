package com.ecommerce.infrastructure.cache

import com.ecommerce.application.port.CachePort
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import redis.clients.jedis.JedisPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RedisCachePort(
    private val jedisPool: JedisPool,
    private val json: Json
) : CachePort {

    override suspend fun <T : Any> set(key: String, value: T, ttlSeconds: Long) {
        withContext(Dispatchers.IO) {
            jedisPool.resource.use { jedis ->
                val serialized = json.encodeToString(serializer(value::class.java), value)
                jedis.setex(key, ttlSeconds.toInt(), serialized)
            }
        }
    }

    override suspend fun <T : Any> get(key: String, clazz: Class<T>): T? {
        return withContext(Dispatchers.IO) {
            jedisPool.resource.use { jedis ->
                val data = jedis.get(key) ?: return@withContext null
                json.decodeFromString(serializer(clazz.kotix), data)
            }
        }
    }

    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            jedisPool.resource.use { jedis ->
                jedis.del(key)
            }
        }
    }

    override suspend fun exists(key: String): Boolean {
        return withContext(Dispatchers.IO) {
            jedisPool.resource.use { jedis ->
                jedis.exists(key)
            }
        }
    }
}