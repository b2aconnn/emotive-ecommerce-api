package com.loopers.domain.event

import com.loopers.domain.auditlog.EventLog
import org.hibernate.generator.EventType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventHandledRepository : JpaRepository<EventHandled, Long> {
    fun findByEventIdAndEventType(eventId: String, eventType: String): EventHandled?
    fun existsByEventIdAndEventType(eventId: String, eventType: String): Boolean
}
