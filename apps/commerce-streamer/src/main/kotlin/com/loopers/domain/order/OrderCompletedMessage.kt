package com.loopers.domain.order

data class OrderCompletedMessage(
    val orderId: Long,
    val orderItems: List<OrderCompletedItem>,
)
