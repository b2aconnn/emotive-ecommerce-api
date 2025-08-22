package com.loopers.infrastructure.payment.pgclient;

import com.loopers.domain.payment.dto.PaymentRequest;
import com.loopers.domain.payment.dto.PaymentResponse;
import com.loopers.infrastructure.payment.pgclient.config.FeignClientTimeoutConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "pgSimulatorClient",
        url = "http://localhost:8082",
        configuration = FeignClientTimeoutConfig.class
)
public interface PgSimulatorFeignClient {
    @PostMapping("/api/v1/payments")
    PaymentResponse requestPayment(@RequestBody PaymentRequest request);
}
