package com.ecommerce.web.routes

import com.ecommerce.application.service.OrderService
import com.ecommerce.web.model.request.CreateOrderRequest
import com.ecommerce.web.model.response.ErrorResponse
import com.ecommerce.web.model.response.OrderResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.orderRoutes(orderService: OrderService) {
    authenticate("auth-jwt") {
        route("/orders") {
            post {
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("User not authenticated"))

                try {
                    val request = call.receive<CreateOrderRequest>()
                    val order = orderService.createOrder(
                        userId = UUID.fromString(userId),
                        items = request.items.map { it.toDomain() }
                    )
                    call.respond(HttpStatusCode.Created, OrderResponse.fromDomain(order))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Failed to create order"))
                }
            }

            get {
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("User not authenticated"))

                val orders = orderService.getUserOrders(UUID.fromString(userId))
                call.respond(orders.map { OrderResponse.fromDomain(it) })
            }

            delete("/{id}") {
                val userId = call.principal<UserIdPrincipal>()?.name
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse("User not authenticated"))

                val role = call.request.headers["X-User-Role"]
                val orderId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid order ID"))

                try {
                    val cancelled = orderService.cancelOrder(
                        orderId = orderId,
                        userId = UUID.fromString(userId),
                        isAdmin = role == "ADMIN"
                    )

                    if (cancelled) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Failed to cancel order"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Failed to cancel order"))
                }
            }
        }

        // Admin stats route
        get("/stats/orders") {
            val role = call.request.headers["X-User-Role"]

            if (role != "ADMIN") {
                return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse("Admin access required"))
            }

            val startDate = call.request.queryParameters["start_date"]
            val endDate = call.request.queryParameters["end_date"]

            val stats = orderService.getOrderStats(startDate, endDate)
            call.respond(stats)
        }
    }
}