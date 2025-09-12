package com.loopers.domain.order.application

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.loopers.domain.order.dto.OrderCompletedMessage
import com.loopers.domain.order.dto.OrderMessageEnvelope
import com.loopers.domain.product.ProductMetrics
import com.loopers.domain.product.ProductMetricsRepository
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OrderService(
    val productMetricsRepository: ProductMetricsRepository,
    val jacksonObjectMapper: ObjectMapper,
    val redisTemplate: RedisTemplate<String, String>,
) {
    @Transactional
    fun processOrderEvent(envelope: OrderMessageEnvelope) {
        saveMetric(envelope.payload)
        saveRanking()
    }

    fun saveMetric(
        payload: JsonNode
    ) {
        var metrics: ProductMetrics? = null
        val payload = jacksonObjectMapper.treeToValue(payload, OrderCompletedMessage::class.java)
        for (item in payload.orderItems) {
            metrics = productMetricsRepository.findByProductId(item.productId)
                ?: ProductMetrics(productId = item.productId)
            metrics.soldCount(item.quantity)
        }

        metrics?.let { productMetricsRepository.save(it) }
    }

    fun addScoresBatch(entries: Map<String, Double>) {
        val dateKey = "ranking:order:${LocalDate.now()}"  // ex) ranking:order:2025-09-12

        val typedTuples = entries.map { (productId, score) ->
            ZSetOperations.TypedTuple.of(productId, score)
        }.toSet()

        redisTemplate.opsForZSet().add("ranking:order", typedTuples)
    }

    private fun saveRanking() {
        // TODO
    }
}
