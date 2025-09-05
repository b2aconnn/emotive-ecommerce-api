package com.loopers.domain.auditlog

import java.time.ZonedDateTime

data class AuditMessageEnvelope(
    val eventType: String,
    val eventId: String,
    val aggregateId: String,
    val aggregateType: String,
    val occurredAt: ZonedDateTime,
    val payload: String
)
