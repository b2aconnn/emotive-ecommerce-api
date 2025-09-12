package com.loopers.domain.order.dto

data class OrderCompletedMessage(
    val orderId: Long,
    val orderItems: List<OrderCompletedItem>,
)
