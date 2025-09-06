package com.loopers.domain.product

data class ProductLikeRemovedMessage(
    val productId: Long,
    val quantity: Long
)
