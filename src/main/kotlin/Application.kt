package com.ecommerce.bootstrap

import com.ecommerce.application.service.AuthService
import com.ecommerce.application.service.OrderService
import com.ecommerce.application.service.ProductService
import com.ecommerce.infrastructure.cache.RedisCachePort
import com.ecommerce.infrastructure.config.DatabaseConfig
import com.ecommerce.infrastructure.database.entity.*
import com.ecommerce.infrastructure.messaging.RabbitMQPort
import com.ecommerce.infrastructure.repository.OrderRepositoryImpl
import com.ecommerce.infrastructure.repository.ProductRepositoryImpl
import com.ecommerce.infrastructure.repository.UserRepositoryImpl
import com.ecommerce.web.config.JwtConfig
import com.ecommerce.web.routes.authRoutes
import com.ecommerce.web.routes.orderRoutes
import com.ecommerce.web.routes.productRoutes
import com.rabbitmq.client.ConnectionFactory
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Load configuration
    val config = environment.config

    // Initialize JSON
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    // Initialize database
    val database = DatabaseConfig.initDatabase(
        jdbcUrl = config.property("database.url").getString(),
        username = config.property("database.user").getString(),
        password = config.property("database.password").getString()
    )

    // Initialize Redis
    val jedisPool = JedisPool(
        JedisPoolConfig(),
        config.property("redis.host").getString(),
        config.property("redis.port").getString().toInt(),
        2000,
        config.propertyOrNull("redis.password")?.getString()
    )

    // Initialize RabbitMQ
    val rabbitFactory = ConnectionFactory().apply {
        host = config.property("rabbitmq.host").getString()
        port = config.property("rabbitmq.port").getString().toInt()
        username = config.property("rabbitmq.username").getString()
        password = config.property("rabbitmq.password").getString()
        automaticRecoveryEnabled = true
    }
    val rabbitConnection = rabbitFactory.newConnection(
        Executors.newCachedThreadPool()
    )

    // Initialize JWT
    val jwtConfig = JwtConfig(
        secret = config.property("jwt.secret").getString(),
        issuer = config.property("jwt.issuer").getString(),
        audience = config.property("jwt.audience").getString(),
        realm = config.property("jwt.realm").getString(),
        expirationTime = config.property("jwt.expiration").getString().toLong()
    )

    // Initialize repositories
    val userRepository = UserRepositoryImpl()
    val productRepository = ProductRepositoryImpl()
    val orderRepository = OrderRepositoryImpl()

    // Initialize ports
    val cachePort = RedisCachePort(jedisPool, json)
    val messageQueuePort = RabbitMQPort(rabbitConnection, json)

    // Initialize services
    val authService = AuthService(userRepository)
    val productService = ProductService(productRepository, cachePort)
    val orderService = OrderService(orderRepository, productRepository, messageQueuePort)

    // Start message queue consumer
    startMessageConsumer(messageQueuePort)

    // Install plugins
    install(ContentNegotiation) {
        json(json)
    }

    install(CallLogging) {
        level = mu.KotlinLogging.Level.INFO
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtConfig.verifier())
            realm = jwtConfig.realm
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired token")
            }
        }
    }

    // Install Swagger
    install(io.ktor.server.plugins.swagger.SwaggerPlugin) {
        path = "openapi"
        swaggerFile = "openapi.yaml"
    }

    // Routing
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi.yaml")

        authRoutes(authService, jwtConfig)
        productRoutes(productService)
        orderRoutes(orderService)

        get("/health") {
            call.respond(mapOf("status" to "healthy", "timestamp" to System.currentTimeMillis()))
        }
    }
}

fun startMessageConsumer(messageQueuePort: RabbitMQPort) {
    messageQueuePort.subscribe { event ->
        when (event) {
            is com.ecommerce.application.port.DomainEvent.OrderCreated -> {
                logger.info { "Order created: ${event.orderId} for user ${event.userId}" }
                // Simulate email sending
                simulateEmail(
                    to = "user@example.com",
                    subject = "Order Confirmation",
                    body = "Your order ${event.orderId} has been created. Total: ${event.totalAmount}"
                )
            }
            is com.ecommerce.application.port.DomainEvent.OrderCancelled -> {
                logger.info { "Order cancelled: ${event.orderId}" }
                simulateEmail(
                    to = "user@example.com",
                    subject = "Order Cancelled",
                    body = "Your order ${event.orderId} has been cancelled"
                )
            }
            is com.ecommerce.application.port.DomainEvent.ProductStockUpdated -> {
                logger.info { "Product stock updated: ${event.productId} new stock: ${event.newStock}" }
            }
        }
    }
}

fun simulateEmail(to: String, subject: String, body: String) {
    logger.info { "📧 Sending email to $to - Subject: $subject - Body: $body" }
}