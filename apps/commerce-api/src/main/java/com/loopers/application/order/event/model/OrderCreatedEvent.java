package com.loopers.application.order.event.model;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,

        Long couponId,
        Long totalAmount,

        PaymentMethod paymentMethod,
        CardType cardType,
        String cardNo
) {}
