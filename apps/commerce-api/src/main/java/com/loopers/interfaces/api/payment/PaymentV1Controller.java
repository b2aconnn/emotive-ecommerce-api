package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentAppService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentAppService paymentAppService;

    @PostMapping("/callback")
    @Override
    public ApiResponse<String> callback(
            @RequestBody PaymentV1Dto.PgResultRequest pgResultRequest
    ) {
        paymentAppService.processPayment(pgResultRequest.toCommand());
        return ApiResponse.success("success");
    }

    @PostMapping("/order-payment")
    @Override
    public ApiResponse<String> orderPayment(
            @RequestBody PaymentV1Dto.OrderPaymentRequest orderPaymentRequest
    ) {
        paymentAppService.requestPayment(orderPaymentRequest.toCommand());
        return ApiResponse.success("success");
    }
}
