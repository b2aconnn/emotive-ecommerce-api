package com.loopers.interfaces.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.loopers.config.kafka.KafkaConfig
import com.loopers.domain.auditlog.EventLog
import com.loopers.domain.auditlog.EventLogRepository
import com.loopers.domain.auditlog.AuditMessageEnvelope
import com.loopers.domain.order.OrderCompletedMessage
import com.loopers.domain.order.OrderMessageEnvelope
import com.loopers.domain.product.ProductLikeAddedMessage
import com.loopers.domain.product.ProductLikeRemovedMessage
import com.loopers.domain.product.ProductMessageEnvelope
import com.loopers.domain.product.ProductMetrics
import com.loopers.domain.product.ProductMetricsRepository
import com.loopers.domain.product.ProductViewedMessage
import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import jdk.internal.platform.Container.metrics
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import kotlin.jvm.java

@Component
class DemoKafkaConsumer(
    private val eventLogRepository: EventLogRepository,
    private val productMetricsRepository: ProductMetricsRepository,
    private val jacksonObjectMapper: ObjectMapper
) {
    @KafkaListener(
        topics = ["\${kafka.topics.product}"],
        containerFactory = KafkaConfig.BATCH_LISTENER,
    )
    fun orderListener(
        message: String,
        acknowledgment: Acknowledgment,
    ) {
        val envelope = jacksonObjectMapper.readValue(message, OrderMessageEnvelope::class.java)

        var metrics: ProductMetrics? = null
        when (envelope.aggregateType) {
            "ORDER_COMPLETED" -> {
                val payload = jacksonObjectMapper.readValue(envelope.payload, OrderCompletedMessage::class.java)

                for (item in payload.orderItems) {
                    metrics = productMetricsRepository.findByProductId(item.productId)
                        ?: ProductMetrics(productId = item.productId)
                    metrics.soldCount(item.quantity)
                }

                metrics?.let { productMetricsRepository.save(it) }
            }
            else -> {
                println("Unknown event type: ${envelope.eventType}")
            }
        }

        acknowledgment.acknowledge() // manual ack
    }

    @KafkaListener(
        topics = ["\${kafka.topics.product}"],
        containerFactory = KafkaConfig.BATCH_LISTENER,
    )
    fun productListener(
        message: String,
        acknowledgment: Acknowledgment,
    ) {
        val envelope = jacksonObjectMapper.readValue(message, ProductMessageEnvelope::class.java)

        when (envelope.aggregateType) {
            "LIKE_ADDED" -> {
                val payload = jacksonObjectMapper.readValue(envelope.payload, ProductLikeAddedMessage::class.java)

                val metrics = productMetricsRepository.findByProductId(payload.productId)
                    ?: ProductMetrics(productId = payload.productId)

                metrics.increaseLike()
                productMetricsRepository.save(metrics)
            }
            "LIKE_REMOVED" -> {
                val payload = jacksonObjectMapper.readValue(envelope.payload, ProductLikeRemovedMessage::class.java)

                val metrics = productMetricsRepository.findByProductId(payload.productId)
                    ?: ProductMetrics(productId = payload.productId)

                metrics.decreaseLike()
                productMetricsRepository.save(metrics)
            }
            "LIKE_VIEWED" -> {
                val payload = jacksonObjectMapper.readValue(envelope.payload, ProductViewedMessage::class.java)

                val metrics = productMetricsRepository.findByProductId(payload.productId)
                    ?: ProductMetrics(productId = payload.productId)

                metrics.viewCount()
                productMetricsRepository.save(metrics)
            }
            else -> {
                println("Unknown event type: ${envelope.eventType}")
            }
        }

        acknowledgment.acknowledge() // manual ack
    }

    @KafkaListener(
        topics = ["\${kafka.topics.audit}"],
        containerFactory = KafkaConfig.BATCH_LISTENER,
    )
    fun logAuditListener(
        message: AuditMessageEnvelope,
        acknowledgment: Acknowledgment,
    ) {
        val eventLog = EventLog(
            eventId = message.eventId,
            eventType = message.eventType,
            aggregateType = message.aggregateType,
            aggregateId = message.aggregateId,
            payload = message.payload
        )
        eventLogRepository.save(eventLog)

        acknowledgment.acknowledge() // manual ack
    }
}
