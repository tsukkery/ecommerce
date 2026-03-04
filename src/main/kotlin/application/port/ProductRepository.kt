package com.ecommerce.application.port

import com.ecommerce.domain.model.Product
import java.util.*

interface ProductRepository {
    suspend fun create(product: Product): Product
    suspend fun findById(id: UUID): Product?
    suspend fun findAll(limit: Int, offset: Int): List<Product>
    suspend fun update(product: Product): Product
    suspend fun delete(id: UUID): Boolean
    suspend fun count(): Long
}