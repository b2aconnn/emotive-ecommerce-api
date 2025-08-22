package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.PaymentRequest;
import com.loopers.domain.payment.dto.PaymentResponse;

public interface PgClient {
    PaymentResponse requestPayment(PaymentRequest request);
}
