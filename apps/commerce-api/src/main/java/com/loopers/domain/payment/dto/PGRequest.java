package com.loopers.domain.payment.dto;

import com.loopers.domain.payment.PaymentMethod;

public record PGRequest(
    String orderId,
    PaymentMethod paymentMethod,
    CardType cardType,
    String cardNo,
    Long amount,
    String callbackUrl
) {}
