package com.loopers.domain.payment;

import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.PaymentStatus;
import com.loopers.domain.payment.vo.PGTransactionInfoResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.loopers.application.payment.dto.PaymentResultStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final OrderService orderService;

    private final PgClient pgClient;

    public void saveOrderPaymentRequest(PaymentCreateCommand createCommand) {
        paymentRepository.save(Payment.create(
                createCommand.orderId(),
                createCommand.pgOrderId(),
                createCommand.paymentMethod(),
                createCommand.amount()));
    }

    public void handlePaymentResult(Payment payment) {
        PGTransactionInfoResult transactionInfoResult = pgClient.getTransaction(payment.getTransactionKey());

        PaymentResultStatus status = transactionInfoResult.status();
        String transactionKey = transactionInfoResult.transactionKey();
        String reason = transactionInfoResult.reason();

        if (PENDING.equals(status)) {
            payment.updateResult(transactionKey, PaymentStatus.PENDING, reason);
        } else if (SUCCESS.equals(status)) {
            payment.updateResult(transactionKey, PaymentStatus.SUCCESS, reason);
        } else if (FAILED.equals(status)) {
            payment.updateResult(transactionKey, PaymentStatus.FAILED, reason);
            orderService.cancelOrderWithRestoration(payment.getOrderId());
        } else {
            log.error("알 수 없는 결제 상태: {}", status);
            throw new IllegalArgumentException("알 수 없는 결제 상태입니다: " + status);
        }
    }

    public void recoverPendingPayments() {
        log.info("Starting recovery of pending payments");

        final int size = 50;
        final int offset = 0;
        ZonedDateTime tenMinutesAgo = ZonedDateTime.now().minusMinutes(10);
        List<Payment> pendingPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING,
                        tenMinutesAgo,
                        size,
                        offset)
                .orElse(new ArrayList<>());

        log.info("Found {} pending payments to recover", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                handlePaymentResult(payment);
            } catch (Exception e) {
                log.error("Error recovering payment for orderId: {}, pgOrderId: {}: {}", payment.getOrderId(), payment.getPgOrderId(), e.getMessage(), e);
            }
        }

        log.info("Completed recovery of pending payments");
    }
}
