package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentAppService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.resolver.UserContextHolder;
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
        // 현재 PG Simulator 에서 사용자 인증을 요구하고 있는데 callback이라 사용자 인증이 불가능함.
        UserContextHolder.setUserId("authUserId");
        paymentAppService.processPayment(pgResultRequest.toCommand());

        return ApiResponse.success("success");
    }

    // 주문 결제 요청 api
    @PostMapping("/order-payment")
    @Override
    public ApiResponse<String> orderPayment(
            @RequestBody PaymentV1Dto.OrderPaymentRequest orderPaymentRequest
    ) {
        paymentAppService.requestPayment(orderPaymentRequest.toCommand());
        return ApiResponse.success("success");
    }
}
