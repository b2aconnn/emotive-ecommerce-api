package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.dto.PaymentOrderPaymentRequest;
import com.loopers.interfaces.api.payment.dto.PaymentPgResultRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment API", description = "Payment API 입니다.")
public interface PaymentApiSpec {

    @Operation(
            summary = "주문 결제 요청 콜백",
            description = "주문 결제 요청에 대한 콜백을 받습니다."
    )
    ApiResponse<String> callback(
            @RequestBody PaymentPgResultRequest pgResultRequest
    );

    @Operation(
            summary = "주문 결제 요청",
            description = "주문 결제를 요청합니다."
    )
    ApiResponse<String> orderPayment(
            @RequestBody PaymentOrderPaymentRequest orderPaymentRequest
    );
}
