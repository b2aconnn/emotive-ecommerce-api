package com.loopers.domain.payment;

import com.loopers.application.order.PaymentStatusResult;
import com.loopers.domain.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PGService {

    private final PaymentRepository paymentRepository;

    private final PgClient pgClient;

    @Async
    @Transactional
    public void requestPayment(Long orderId, PaymentRequest request) {
        paymentRepository.save(Payment.create(
                orderId, request.paymentMethod(), request.amount()));

        pgClient.requestPayment(request);
    }

    public PaymentStatusResult getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("결제 정보가 존재하지 않습니다."));

        return new PaymentStatusResult(payment.getStatus());
    }
}
