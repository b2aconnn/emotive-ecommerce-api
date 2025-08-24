package com.loopers.infrastructure.payment.pgclient.dto;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

public record PGSimulatorRequest(
    String orderId,
    PaymentMethod paymentMethod,
    CardType cardType,
    String cardNo,
    Long amount,
    String callbackUrl
) {}
