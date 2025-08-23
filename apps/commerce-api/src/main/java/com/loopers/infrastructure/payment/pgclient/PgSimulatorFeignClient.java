package com.loopers.infrastructure.payment.pgclient;

import com.loopers.domain.payment.dto.PGPaymentRequestResponse;
import com.loopers.domain.payment.dto.PGRequest;
import com.loopers.domain.payment.dto.PGTransactionInfoResponse;
import com.loopers.infrastructure.payment.pgclient.config.FeignClientTimeoutConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "pgSimulatorClient",
        url = "http://localhost:8082",
        configuration = FeignClientTimeoutConfig.class
)
public interface PgSimulatorFeignClient {
    @PostMapping("/api/v1/payments")
    PGPaymentRequestResponse requestPayment(@RequestBody PGRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PGTransactionInfoResponse getTransaction(@PathVariable String transactionKey);
}
