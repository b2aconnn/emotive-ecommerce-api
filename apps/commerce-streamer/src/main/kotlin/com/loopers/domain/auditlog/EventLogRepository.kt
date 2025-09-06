package com.loopers.domain.auditlog

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventLogRepository : JpaRepository<EventLog, Long> {
}
