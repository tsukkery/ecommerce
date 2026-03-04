package com.ecommerce.infrastructure.messaging

import com.ecommerce.application.port.DomainEvent
import com.ecommerce.application.port.MessageQueuePort
import com.rabbitmq.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

class RabbitMQPort(
    private val connection: Connection,
    private val json: Json,
    private val exchangeName: String = "ecommerce.events",
    private val queueName: String = "order_events"
) : MessageQueuePort {

    private val channel by lazy { connection.createChannel() }

    init {
        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, true)
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, exchangeName, "order.#")
    }

    override suspend fun publish(event: DomainEvent) {
        withContext(Dispatchers.IO) {
            try {
                val routingKey = when (event) {
                    is DomainEvent.OrderCreated -> "order.created"
                    is DomainEvent.OrderCancelled -> "order.cancelled"
                    is DomainEvent.ProductStockUpdated -> "product.stock.updated"
                }

                val message = json.encodeToString(event)
                channel.basicPublish(exchangeName, routingKey, null, message.toByteArray())
                logger.info { "Published event: $routingKey" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to publish event" }
                throw e
            }
        }
    }

    override suspend fun subscribe(handler: (DomainEvent) -> Unit) {
        withContext(Dispatchers.IO) {
            val consumer = object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray
                ) {
                    try {
                        val message = String(body)
                        val event = when (envelope.routingKey) {
                            "order.created" -> json.decodeFromString<DomainEvent.OrderCreated>(message)
                            "order.cancelled" -> json.decodeFromString<DomainEvent.OrderCancelled>(message)
                            "product.stock.updated" -> json.decodeFromString<DomainEvent.ProductStockUpdated>(message)
                            else -> null
                        }

                        if (event != null) {
                            handler(event)
                            channel.basicAck(envelope.deliveryTag, false)
                        } else {
                            channel.basicNack(envelope.deliveryTag, false, false)
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Error processing message" }
                        channel.basicNack(envelope.deliveryTag, false, true)
                    }
                }
            }

            channel.basicConsume(queueName, false, consumer)
        }
    }
}