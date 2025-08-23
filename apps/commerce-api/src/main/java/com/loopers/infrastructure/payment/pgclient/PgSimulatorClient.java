package com.loopers.infrastructure.payment.pgclient;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PGPaymentRequestResponse;
import com.loopers.domain.payment.dto.PGRequest;
import com.loopers.domain.payment.dto.PGTransactionInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PgSimulatorClient implements PgClient {

    private final PgSimulatorFeignClient pgSimulatorFeignClient;

    @CircuitBreaker(name = "pg-request-payment", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pg-request-payment-retry", fallbackMethod = "requestPaymentFallback")
    @Override
    public PGPaymentRequestResponse requestPayment(PGRequest request) {
        PGPaymentRequestResponse pgPaymentRequestResponse = pgSimulatorFeignClient.requestPayment(request);
        return pgPaymentRequestResponse;
    }

    @Override
    public PGTransactionInfoResponse getTransaction(String transactionKey) {
        return pgSimulatorFeignClient.getTransaction(transactionKey);
    }
}
