package com.loopers.infrastructure.payment.pgclient;

import com.loopers.infrastructure.payment.pgclient.config.FeignClientTimeoutConfig;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorRequest;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorRequestResponse;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorTransactionInfoResponse;
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
    PGSimulatorRequestResponse requestPayment(@RequestBody PGSimulatorRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PGSimulatorTransactionInfoResponse getTransaction(@PathVariable String transactionKey);
}
