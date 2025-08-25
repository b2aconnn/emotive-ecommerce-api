package com.loopers.infrastructure.payment.scheduler;

import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentRecoveryScheduler {

    private final PaymentService paymentService;

    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void recoverPendingPayments() {
        log.info("Start payment status recovery scheduler");

        try {
            paymentService.recoverPendingPayments();
            log.info("End payment status recovery scheduler");
        } catch (Exception e) {
            log.error("Error during payment status recovery: {}", e.getMessage(), e);
        }
    }
}
