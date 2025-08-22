package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment V1 API", description = "Payment API 입니다.")
public interface PaymentV1ApiSpec {

    @Operation(
            summary = "주문 결제 요청 콜백",
            description = "주문 결제 요청에 대한 콜백을 받습니다."
    )
    ApiResponse<String> callback(
            @RequestBody PaymentV1Dto.PgPaymentResultRequest pgResultRequest
    );
}
