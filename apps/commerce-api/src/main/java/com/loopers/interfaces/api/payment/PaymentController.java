package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.dto.PaymentOrderPaymentRequest;
import com.loopers.interfaces.api.payment.dto.PaymentPgResultRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController implements PaymentApiSpec {

    private final PaymentService paymentService;

    @Override
    @PostMapping("/callback")
    public ApiResponse<String> callback(
            @RequestBody PaymentPgResultRequest pgResultRequest) {
        paymentService.processPayment(pgResultRequest.toCommand());
        return ApiResponse.success("success");
    }

    @Override
    @PostMapping("/order-payment")
    public ApiResponse<String> orderPayment(
            @RequestBody PaymentOrderPaymentRequest orderPaymentRequest) {
        paymentService.requestPayment(orderPaymentRequest.toCommand());
        return ApiResponse.success("success");
    }
}
