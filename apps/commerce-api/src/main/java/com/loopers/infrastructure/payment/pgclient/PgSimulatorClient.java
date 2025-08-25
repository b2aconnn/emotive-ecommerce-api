package com.loopers.infrastructure.payment.pgclient;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PGRequest;
import com.loopers.domain.payment.vo.PGRequestResult;
import com.loopers.domain.payment.vo.PGTransactionInfoResult;
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
    public PGRequestResult requestPayment(PGRequest request) {
        PGSimulatorRequestResponse pgSimulatorRequestResponse =
                pgSimulatorFeignClient.requestPayment(request.toSimulatorRequest());

        return PGRequestResult.from(pgSimulatorRequestResponse);
    }

    @CircuitBreaker(name = "defaultConfig", fallbackMethod = "getTransactionFallback")
    @Retry(name = "defaultConfig", fallbackMethod = "getTransactionFallback")
    @Override
    public PGTransactionInfoResult getTransaction(String transactionKey) {
        PGSimulatorTransactionInfoResponse transactionInfoResponse = pgSimulatorFeignClient.getTransaction(transactionKey);
        return PGTransactionInfoResult.from(transactionInfoResponse);
    }

    public PGRequestResult requestPaymentFallback(PGRequest request, Throwable throwable) {
        log.error("결제 요청 실패: {}", throwable.getMessage());
        return new PGRequestResult("", FAILED, "결제 요청 실패");
    }

    public PGTransactionInfoResult getTransactionFallback(String transactionKey, Throwable throwable) {
        log.error("거래 조회 실패: transactionKey : {}, error : {}", transactionKey, throwable.getMessage());
        return new PGTransactionInfoResult(
                "",
                null,
                null,
                null,
                null,
                FAILED,
                "거래 조회 실패");
    }
}

