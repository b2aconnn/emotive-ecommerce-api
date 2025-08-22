package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentResultCommand;
import com.loopers.application.payment.dto.PaymentStatusResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.PaymentStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.loopers.application.payment.dto.PaymentResultStatus.FAILED;
import static com.loopers.application.payment.dto.PaymentResultStatus.SUCCESS;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentAppService {

    private final PaymentRepository paymentRepository;

    private final OrderService orderService;

    public PaymentStatusResult getPaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        return new PaymentStatusResult(orderId, payment.getStatus());
    }

    @Transactional
    public void processPayment(PaymentResultCommand resultCommand) {
        Payment payment = paymentRepository.findByPgOrderId(resultCommand.orderId())
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        handlePaymentResult(resultCommand, payment);
    }

    private void handlePaymentResult(PaymentResultCommand resultCommand, Payment payment) {
        if (SUCCESS.equals(resultCommand.status())) {
            payment.updateResult(
                    resultCommand.transactionKey(),
                    PaymentStatus.SUCCESS,
                    resultCommand.reason());
        } else if (FAILED.equals(resultCommand.status())) {
            payment.updateResult(
                    resultCommand.transactionKey(),
                    PaymentStatus.FAILED,
                    resultCommand.reason());

            orderService.cancelOrderWithRestoration(payment.getOrderId());
        } else {
            log.error("알 수 없는 결제 상태: {}", resultCommand.status());
            throw new IllegalArgumentException("알 수 없는 결제 상태입니다: " + resultCommand.status());
        }
    }
}
