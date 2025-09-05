package com.loopers.domain.product

data class ProductLikeAddedMessage(
    val productId: Long,
    val quantity: Long
)
