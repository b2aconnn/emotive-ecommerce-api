package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.application.payment.dto.PaymentOrderCommand;
import com.loopers.application.payment.dto.PaymentResultCommand;
import com.loopers.application.payment.dto.PaymentStatusResult;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.vo.PGRequestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentAppService {

    private final PaymentRepository paymentRepository;

    private final PaymentService paymentService;

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
        PGRequestResult pgRequestResult = pgClient.requestPayment(paymentCommand.toPGRequest(
                pgOrderId,
                payment.getAmount(),
                callbackUrl));

        pgRequestResult.checkFailed();

        payment.updateTransactionKey(pgRequestResult.transactionKey());
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

        paymentService.handlePaymentResult(payment);
    }
}
