package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Order V1 API", description = "Order API 입니다.")
public interface OrderV1ApiSpec {
    @Operation(
            summary = "주문 생성",
            description = "주문을 생성합니다."
    )
    ApiResponse<OrderV1Dto.CreateResponse> create(
            @RequestBody OrderV1Dto.CreateRequest createRequest
    );

    @Operation(
            summary = "주문 결제 상태 조회",
            description = "주문 결제 상태를 조회합니다."
    )
    ApiResponse<OrderV1Dto.StatusResponse> getStatus(
            @PathVariable(value = "orderId") Long orderId);
}
