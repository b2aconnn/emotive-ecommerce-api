package com.loopers.infrastructure.payment.pgclient;

import org.springframework.stereotype.Component;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PgRequest;
import com.loopers.domain.payment.vo.PgRequestResult;
import com.loopers.domain.payment.vo.PgTransactionInfoResult;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorRequestResponse;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorTransactionInfoResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgSimulatorClient implements PgClient {

    private final PgSimulatorFeignClient pgSimulatorFeignClient;

    @CircuitBreaker(name = "pgRequestPaymentConfig", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pgRequestPaymentRetryConfig", fallbackMethod = "requestPaymentFallback")
    @Override
    public PgRequestResult requestPayment(PgRequest request) {
        PgSimulatorRequestResponse pgSimulatorRequestResponse =
                pgSimulatorFeignClient.requestPayment(request.toSimulatorRequest());

        return PgRequestResult.from(pgSimulatorRequestResponse);
    }

    @CircuitBreaker(name = "defaultConfig", fallbackMethod = "getTransactionFallback")
    @Retry(name = "defaultConfig", fallbackMethod = "getTransactionFallback")
    @Override
    public PgTransactionInfoResult getTransaction(String transactionKey) {
        PgSimulatorTransactionInfoResponse transactionInfoResponse =
                pgSimulatorFeignClient.getTransaction(transactionKey);
        return PgTransactionInfoResult.from(transactionInfoResponse);
    }

    public PgRequestResult requestPaymentFallback(PgRequest request, Throwable throwable) {
        log.error("결제 요청 실패: {}", throwable.getMessage());
        return new PgRequestResult("", PaymentResultStatus.FAILED, "결제 요청 실패");
    }

    public PgTransactionInfoResult getTransactionFallback(String transactionKey, Throwable throwable) {
        log.error("거래 조회 실패: transactionKey : {}, error : {}", transactionKey, throwable.getMessage());
        return new PgTransactionInfoResult("", null, null, null, null, PaymentResultStatus.FAILED, "거래 조회 실패");
    }
}
