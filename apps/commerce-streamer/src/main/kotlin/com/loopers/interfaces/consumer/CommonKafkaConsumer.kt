package com.loopers.interfaces.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.loopers.config.kafka.KafkaConfig
import com.loopers.domain.auditlog.AuditMessageEnvelope
import com.loopers.domain.auditlog.EventLog
import com.loopers.domain.auditlog.EventLogRepository
import com.loopers.domain.event.EventHandled
import com.loopers.domain.event.EventHandledRepository
import com.loopers.domain.order.OrderCompletedMessage
import com.loopers.domain.order.OrderMessageEnvelope
import com.loopers.domain.product.*
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class CommonKafkaConsumer(
    private val eventLogRepository: EventLogRepository,
    private val productMetricsRepository: ProductMetricsRepository,
    private val eventHandledRepository: EventHandledRepository,
    private val jacksonObjectMapper: ObjectMapper
) {
    @KafkaListener(
        topics = ["\${kafka.topics.order}"],
        containerFactory = KafkaConfig.BATCH_LISTENER,
    )
    fun orderListener(
        message: String,
        acknowledgment: Acknowledgment,
    ) {
        val envelope = jacksonObjectMapper.readValue(message, OrderMessageEnvelope::class.java)

        if (eventHandledRepository.existsByEventIdAndEventType(
                envelope.eventId, envelope.eventType)) {
            return
        }

        var metrics: ProductMetrics? = null
        when (envelope.eventType) {
            "ORDER_COMPLETED" -> {
                val payload = jacksonObjectMapper.treeToValue(envelope.payload, OrderCompletedMessage::class.java)

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

        eventHandledRepository.save(EventHandled(envelope.eventId, envelope.eventType))

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

        if (eventHandledRepository.existsByEventIdAndEventType(
                envelope.eventId, envelope.eventType)) {
            return
        }

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

        eventHandledRepository.save(EventHandled(envelope.eventId, envelope.eventType))

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
        if (eventHandledRepository.existsByEventIdAndEventType(
                message.eventId, message.eventType)) {
            return
        }

        val eventLog = EventLog(
            eventId = message.eventId,
            eventType = message.eventType,
            aggregateType = message.aggregateType,
            aggregateId = message.aggregateId,
            payload = message.payload
        )
        eventLogRepository.save(eventLog)

        eventHandledRepository.save(EventHandled(message.eventId, message.eventType))

        acknowledgment.acknowledge() // manual ack
    }
}
