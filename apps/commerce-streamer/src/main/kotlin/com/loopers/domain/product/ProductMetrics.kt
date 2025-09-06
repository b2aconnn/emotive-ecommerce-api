package com.loopers.domain.product

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "product_metrics")
class ProductMetrics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val productId: Long,
    var likeCount: Long = 0,
    var viewCount: Long = 0,
    var soldCount: Long = 0,

    var lastUpdatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun increaseLike() {
        likeCount += 1
    }

    fun decreaseLike() {
        likeCount -= 1
    }

    fun viewCount() {
        viewCount += 1
    }

    fun soldCount(quantity: Long) {
        soldCount += quantity
    }
}
