package com.loopers.domain.order

import java.time.ZonedDateTime

data class OrderMessageEnvelope(
    val eventType: String,
    val eventId: String,
    val aggregateId: String,
    val aggregateType: String,
    val occurredAt: ZonedDateTime,
    val payload: String
)
