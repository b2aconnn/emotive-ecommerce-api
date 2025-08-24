package com.loopers.application.payment;

import com.loopers.application.payment.dto.*;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.PaymentStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.dto.PGPaymentRequestResponse;
import com.loopers.domain.payment.dto.PGTransactionInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.loopers.application.payment.dto.PaymentResultStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentAppService {

    private final PaymentRepository paymentRepository;

    private final OrderService orderService;

    private final PgClient pgClient;

    @Value("${pg.callback-url}")
    private String callbackUrl;

    public PaymentStatusResult getPaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        return new PaymentStatusResult(orderId, payment.getStatus());
    }

    @Transactional
    public void requestPayment(PaymentOrderCommand paymentCommand) {
        Payment payment = paymentRepository.findByOrderId(paymentCommand.orderId())
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        String pgOrderId = payment.getPgOrderId();
        PGPaymentRequestResponse pgPaymentRequestResponse = pgClient.requestPayment(paymentCommand.toPGRequest(
                pgOrderId,
                payment.getAmount(),
                callbackUrl));

        if (pgPaymentRequestResponse.status() == FAILED) {
            throw new IllegalStateException("결제 요청에 실패했습니다: " + pgPaymentRequestResponse.reason());
        }

        payment.updateTransactionKey(pgPaymentRequestResponse.transactionKey());
    }

    public void createPayment(PaymentCreateCommand createCommand) {
        paymentRepository.save(Payment.create(
                createCommand.orderId(),
                createCommand.pgOrderId(),
                createCommand.paymentMethod(),
                createCommand.amount()));
    }

    @Transactional
    public void processPayment(PaymentResultCommand resultCommand) {
        Payment payment = paymentRepository.findByPgOrderId(resultCommand.orderId())
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        handlePaymentResult(payment);
    }

    private void handlePaymentResult(Payment payment) {
        PGTransactionInfoResponse transactionInfoResponse = pgClient.getTransaction(payment.getTransactionKey());

        if (payment.getTransactionKey() == null || payment.getTransactionKey().isEmpty()) {
            throw new IllegalStateException("결제 요청이 존재하지 않습니다.");
        }

        String transactionKey = transactionInfoResponse.transactionKey();
        PaymentResultStatus status = transactionInfoResponse.status();
        String reason = transactionInfoResponse.reason();

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
}
