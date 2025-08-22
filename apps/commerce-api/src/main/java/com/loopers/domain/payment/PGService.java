package com.loopers.domain.payment;

import com.loopers.domain.order.PaymentStatus;
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
        Payment savePayment = paymentRepository.save(Payment.create(
                orderId, request.orderId(), request.paymentMethod(), request.amount()));

        try {
            pgClient.requestPayment(request);
        } catch (Exception e) {
            savePayment.updateResult(
                    null,
                    PaymentStatus.FAILED,
                    "결제 요청 실패: " + e.getMessage());
        }
    }
}
