package com.loopers.domain.product

import java.time.ZonedDateTime

data class ProductMessageEnvelope(
    val eventType: String,
    val eventId: String,
    val aggregateId: String,
    val aggregateType: String,
    val occurredAt: ZonedDateTime,
    val payload: String
)
