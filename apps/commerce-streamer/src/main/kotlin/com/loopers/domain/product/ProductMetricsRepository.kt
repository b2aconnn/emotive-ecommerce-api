package com.loopers.domain.product

import com.loopers.domain.auditlog.EventLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductMetricsRepository : JpaRepository<ProductMetrics, Long> {
    fun findByProductId(productId: Long): ProductMetrics?
}
