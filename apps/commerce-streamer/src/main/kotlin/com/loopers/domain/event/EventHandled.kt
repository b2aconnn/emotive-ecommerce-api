package com.loopers.domain.event

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "event_handled")
data class EventHandled(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val eventId: String,
    val eventType: String,
    val handledAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(eventId: String, eventType: String) :
            this(0L, eventId, eventType, LocalDateTime.now())
}
