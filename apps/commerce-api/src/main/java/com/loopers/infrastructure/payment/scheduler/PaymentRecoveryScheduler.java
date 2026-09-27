package com.loopers.infrastructure.payment.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.domain.payment.PaymentReconciliation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code @EnableScheduling} 활성화 이후에도 기본적으로는 동작하지 않는다.
 * 결제 상태 복구는 외부 PG 호출을 동반하므로, 켤 준비가 된 환경에서만
 * {@code scheduler.payment-recovery.enabled=true}로 명시적으로 활성화한다.
 */
@ConditionalOnProperty(value = "scheduler.payment-recovery.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentRecoveryScheduler {

    private final PaymentReconciliation paymentReconciliation;

    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void recoverPendingPayments() {
        log.info("Start payment status recovery scheduler");

        try {
            paymentReconciliation.recoverPendingPayments();
            log.info("End payment status recovery scheduler");
        } catch (Exception e) {
            log.error("Error during payment status recovery: {}", e.getMessage(), e);
        }
    }
}
