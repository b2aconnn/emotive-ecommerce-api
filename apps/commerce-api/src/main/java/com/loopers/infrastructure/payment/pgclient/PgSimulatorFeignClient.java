package com.loopers.infrastructure.payment.pgclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.loopers.infrastructure.payment.pgclient.config.FeignClientConfig;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorRequest;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorRequestResponse;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorTransactionInfoResponse;

@FeignClient(name = "pgSimulatorClient", url = "http://localhost:8082", configuration = FeignClientConfig.class)
public interface PgSimulatorFeignClient {
    @PostMapping("/api/v1/payments")
    PgSimulatorRequestResponse requestPayment(@RequestBody PgSimulatorRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgSimulatorTransactionInfoResponse getTransaction(@PathVariable String transactionKey);
}
