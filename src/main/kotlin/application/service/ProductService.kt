package com.ecommerce.application.service

import com.ecommerce.application.port.ProductRepository
import com.ecommerce.application.port.CachePort
import com.ecommerce.domain.model.Product
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class ProductService(
    private val productRepository: ProductRepository,
    private val cachePort: CachePort
) {
    suspend fun createProduct(name: String, description: String, price: BigDecimal, stock: Int): Product {
        val product = Product(
            id = UUID.randomUUID(),
            name = name,
            description = description,
            price = price,
            stock = stock,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val created = productRepository.create(product)
        cachePort.set("product:${created.id}", created, 300)
        return created
    }

    suspend fun getProduct(id: UUID): Product? {
        val cached = cachePort.get<Product>("product:$id", Product::class.java)
        if (cached != null) {
            return cached
        }

        val product = productRepository.findById(id)
        if (product != null) {
            cachePort.set("product:$id", product, 300)
        }
        return product
    }

    suspend fun getAllProducts(page: Int, size: Int): List<Product> {
        return productRepository.findAll(size, (page - 1) * size)
    }

    suspend fun updateProduct(id: UUID, name: String?, description: String?, price: BigDecimal?, stock: Int?): Product? {
        val existing = productRepository.findById(id) ?: return null

        val updated = existing.copy(
            name = name ?: existing.name,
            description = description ?: existing.description,
            price = price ?: existing.price,
            stock = stock ?: existing.stock,
            updatedAt = LocalDateTime.now()
        )

        val result = productRepository.update(updated)
        cachePort.delete("product:$id")
        return result
    }

    suspend fun deleteProduct(id: UUID): Boolean {
        val deleted = productRepository.delete(id)
        if (deleted) {
            cachePort.delete("product:$id")
        }
        return deleted
    }
}