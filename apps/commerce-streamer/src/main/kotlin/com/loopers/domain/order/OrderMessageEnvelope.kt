package com.loopers.domain.order

import com.fasterxml.jackson.databind.JsonNode

data class OrderMessageEnvelope(
    val eventType: String,
    val eventId: String,
    val aggregateId: String,
    val aggregateType: String,
    val occurredAt: Double,
    val payload: JsonNode
)
