package com.ecommerce.web.routes

import com.ecommerce.application.service.AuthService
import com.ecommerce.web.model.request.LoginRequest
import com.ecommerce.web.model.request.RegisterRequest
import com.ecommerce.web.model.response.AuthResponse
import com.ecommerce.web.model.response.ErrorResponse
import com.ecommerce.web.config.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    authService: AuthService,
    jwtConfig: JwtConfig
) {
    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val user = authService.register(
                    email = request.email,
                    password = request.password,
                    firstName = request.firstName,
                    lastName = request.lastName
                )

                val token = jwtConfig.generateToken(
                    userId = user.id.toString(),
                    email = user.email,
                    role = user.role.name
                )

                call.respond(
                    HttpStatusCode.Created,
                    AuthResponse(
                        token = token,
                        userId = user.id.toString(),
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = user.role.name
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Registration failed"))
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val user = authService.login(request.email, request.password)

                val token = jwtConfig.generateToken(
                    userId = user.id.toString(),
                    email = user.email,
                    role = user.role.name
                )

                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(
                        token = token,
                        userId = user.id.toString(),
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = user.role.name
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(e.message ?: "Login failed"))
            }
        }
    }
}