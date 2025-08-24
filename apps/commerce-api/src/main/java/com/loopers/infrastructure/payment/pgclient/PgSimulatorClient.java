package com.loopers.infrastructure.payment.pgclient;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PGPaymentRequestResponse;
import com.loopers.domain.payment.dto.PGRequest;
import com.loopers.domain.payment.dto.PGTransactionInfoResponse;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorRequestResponse;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorTransactionInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.loopers.application.payment.dto.PaymentResultStatus.FAILED;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgSimulatorClient implements PgClient {

    private final PgSimulatorFeignClient pgSimulatorFeignClient;

    @CircuitBreaker(name = "pgRequestPaymentConfig", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pgRequestPaymentRetryConfig", fallbackMethod = "requestPaymentFallback")
    @Override
    public PGPaymentRequestResponse requestPayment(PGRequest request) {
        PGSimulatorRequestResponse pgSimulatorRequestResponse =
                pgSimulatorFeignClient.requestPayment(request.toSimulatorRequest());

        return PGPaymentRequestResponse.from(pgSimulatorRequestResponse);
    }

    @CircuitBreaker(name = "defaultConfig", fallbackMethod = "getTransactionFallback")
    @Retry(name = "defaultConfig", fallbackMethod = "getTransactionFallback")
    @Override
    public PGTransactionInfoResponse getTransaction(String transactionKey) {
        PGSimulatorTransactionInfoResponse transactionInfoResponse = pgSimulatorFeignClient.getTransaction(transactionKey);
        return PGTransactionInfoResponse.from(transactionInfoResponse);
    }

    public PGPaymentRequestResponse requestPaymentFallback(PGRequest request, Throwable throwable) {
        log.error("결제 요청 실패: {}", throwable.getMessage());
        return new PGPaymentRequestResponse("", FAILED, "결제 요청 실패");
    }

    public PGTransactionInfoResponse getTransactionFallback(String transactionKey, Throwable throwable) {
        log.error("거래 조회 실패: transactionKey : {}, error : {}", transactionKey, throwable.getMessage());
        return new PGTransactionInfoResponse(
                "",
                null,
                null,
                null,
                null,
                FAILED,
                "거래 조회 실패");
    }
}

