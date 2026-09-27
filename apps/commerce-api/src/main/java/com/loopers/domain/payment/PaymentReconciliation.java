package com.loopers.domain.payment;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.order.PaymentStatus;
import com.loopers.domain.payment.vo.PgTransactionInfoResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentReconciliation {

    private static final long RECOVERY_THRESHOLD_MINUTES = 10;

    private final PaymentRepository paymentRepository;

    private final PgClient pgClient;

    public void handlePaymentResult(Payment payment, PgTransactionInfoResult transactionInfoResult) {
        PaymentResultStatus status = transactionInfoResult.status();
        String transactionKey = transactionInfoResult.transactionKey();
        String reason = transactionInfoResult.reason();

        if (PaymentResultStatus.PENDING.equals(status)) {
            payment.updateResult(transactionKey, PaymentStatus.PENDING, reason);
        } else if (PaymentResultStatus.SUCCESS.equals(status)) {
            payment.updateResult(transactionKey, PaymentStatus.SUCCESS, reason);
        } else if (PaymentResultStatus.FAILED.equals(status)) {
            payment.updateResult(transactionKey, PaymentStatus.FAILED, reason);
        } else {
            log.error("알 수 없는 결제 상태: {}", status);

            throw new IllegalArgumentException("알 수 없는 결제 상태입니다: " + status);
        }
    }

    // 복구 배치는 건별 실패 원인(스택트레이스)을 남겨야 하므로 반복문 내 ERROR 로그를 허용한다.
    @SuppressWarnings("checkstyle:logInLoop")
    public void recoverPendingPayments() {
        log.info("Starting recovery of pending payments");

        final int size = 50;
        final int offset = 0;
        ZonedDateTime tenMinutesAgo = ZonedDateTime.now().minusMinutes(RECOVERY_THRESHOLD_MINUTES);
        List<Payment> pendingPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, tenMinutesAgo, size, offset)
                .orElse(new ArrayList<>());

        log.info("Found {} pending payments to recover", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                PgTransactionInfoResult transactionInfoResult = pgClient.getTransaction(payment.getTransactionKey());
                handlePaymentResult(payment, transactionInfoResult);
            } catch (Exception e) {
                log.error(
                        "Error recovering payment for orderId: {}, pgOrderId: {}: {}",
                        payment.getOrderId(),
                        payment.getPgOrderId(),
                        e.getMessage(),
                        e);
            }
        }

        log.info("Completed recovery of pending payments");
    }
}
