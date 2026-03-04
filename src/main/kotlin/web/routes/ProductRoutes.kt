package com.ecommerce.web.routes

import com.ecommerce.application.service.ProductService
import com.ecommerce.web.model.request.CreateProductRequest
import com.ecommerce.web.model.request.UpdateProductRequest
import com.ecommerce.web.model.response.ErrorResponse
import com.ecommerce.web.model.response.ProductResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.productRoutes(productService: ProductService) {
    route("/products") {
        // Public routes
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

            val products = productService.getAllProducts(page, size)
            call.respond(
                products.map { ProductResponse.fromDomain(it) }
            )
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid product ID"))

            val product = productService.getProduct(id)
            if (product == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Product not found"))
            } else {
                call.respond(ProductResponse.fromDomain(product))
            }
        }

        // Admin only routes
        authenticate("auth-jwt") {
            post {
                val userId = call.principal<UserIdPrincipal>()?.name
                val role = call.request.headers["X-User-Role"]

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse("Admin access required"))
                }

                try {
                    val request = call.receive<CreateProductRequest>()
                    val product = productService.createProduct(
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        stock = request.stock
                    )
                    call.respond(HttpStatusCode.Created, ProductResponse.fromDomain(product))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Failed to create product"))
                }
            }

            put("/{id}") {
                val userId = call.principal<UserIdPrincipal>()?.name
                val role = call.request.headers["X-User-Role"]

                if (role != "ADMIN") {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse("Admin access required"))
                }

                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid product ID"))

                try {
                    val request = call.receive<UpdateProductRequest>()
                    val product = productService.updateProduct(
                        id = id,
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        stock = request.stock
                    )

                    if (product == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Product not found"))
                    } else {
                        call.respond(ProductResponse.fromDomain(product))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Failed to update product"))
                }
            }

            delete("/{id}") {
                val userId = call.principal<UserIdPrincipal>()?.name
                val role = call.request.headers["X-User-Role"]

                if (role != "ADMIN") {
                    return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponse("Admin access required"))
                }

                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid product ID"))

                val deleted = productService.deleteProduct(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Product not found"))
                }
            }
        }
    }
}