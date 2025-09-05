package com.loopers.domain.auditlog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "event_log")
class EventLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val eventId: String,
    val eventType: String,
    val aggregateType: String,
    val aggregateId: String,
    val payload: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {}
