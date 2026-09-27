package com.loopers.application.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.application.payment.dto.PaymentOrderCommand;
import com.loopers.application.payment.dto.PaymentResultCommand;
import com.loopers.application.payment.event.model.PaymentResultEvent;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentReconciliation;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.vo.PgRequestResult;
import com.loopers.domain.payment.vo.PgTransactionInfoResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentReconciliation paymentReconciliation;

    private final PgClient pgClient;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${pg.callback-url}")
    private String callbackUrl;

    @Transactional
    public void requestPayment(PaymentOrderCommand paymentCommand) {
        Payment payment = paymentRepository
                .findByOrderId(paymentCommand.orderId())
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        String pgOrderId = payment.getPgOrderId();
        PgRequestResult pgRequestResult =
                pgClient.requestPayment(paymentCommand.toPgRequest(pgOrderId, payment.getAmount(), callbackUrl));

        payment.updateTransactionKey(pgRequestResult.transactionKey());
    }

    @Transactional
    public void processPayment(PaymentResultCommand resultCommand) {
        Payment payment = paymentRepository
                .findByPgOrderId(resultCommand.orderId())
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        PgTransactionInfoResult transactionInfoResult = pgClient.getTransaction(payment.getTransactionKey());
        paymentReconciliation.handlePaymentResult(payment, transactionInfoResult);

        applicationEventPublisher.publishEvent(
                new PaymentResultEvent(payment.getOrderId(), transactionInfoResult.status()));
    }

    @Transactional
    public Payment create(PaymentCreateCommand createCommand) {
        return paymentRepository.save(Payment.create(
                createCommand.orderId(),
                createCommand.pgOrderId(),
                createCommand.paymentMethod(),
                createCommand.amount()));
    }
}
