package com.loopers.infrastructure.payment.pgclient;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PaymentRequest;
import com.loopers.domain.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PgSimulatorClient implements PgClient {

    private final PgSimulatorFeignClient pgSimulatorFeignClient;

    @Override
    public PaymentResponse requestPayment(PaymentRequest request) {
        return pgSimulatorFeignClient.requestPayment(request);
    }
}
