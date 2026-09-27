package com.loopers.infrastructure.external.dataplatform;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformClient {

    public void sendOrderData(Object orderData) {
        log.info("Send order data to data platform: {}", orderData);
    }

    public void sendPaymentData(Object paymentData) {
        log.info("Send payment data to data platform: {}", paymentData);
    }
}
