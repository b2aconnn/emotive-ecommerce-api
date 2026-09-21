package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.order.dto.OrderCreateRequest;
import com.loopers.interfaces.api.order.dto.OrderCreateResponse;
import com.loopers.interfaces.api.order.dto.OrderDetailResponse;
import com.loopers.interfaces.api.order.dto.OrderStatusResponse;
import com.loopers.interfaces.api.order.dto.OrdersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Order API", description = "Order API 입니다.")
public interface OrderApiSpec {
    @Operation(
            summary = "주문 생성",
            description = "주문을 생성합니다."
    )
    ApiResponse<OrderCreateResponse> create(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)")
            Long userId,
            @RequestBody OrderCreateRequest createRequest
    );

    @Operation(
            summary = "주문 결제 상태 조회",
            description = "주문 결제 상태를 조회합니다."
    )
    ApiResponse<OrderStatusResponse> getStatus(
            @PathVariable(value = "orderId") Long orderId);

    @Operation(
            summary = "유저의 주문 목록 조회",
            description = "요청자의 주문 목록을 조회합니다."
    )
    ApiResponse<List<OrdersResponse>> getOrders(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)")
            Long userId);

    @Operation(
            summary = "단일 주문 상세 조회",
            description = "요청자 소유의 주문 상세 정보를 조회합니다."
    )
    ApiResponse<OrderDetailResponse> getOrder(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)")
            Long userId,
            @Schema(name = "주문 ID", description = "조회할 주문 ID")
            Long orderId);
}
