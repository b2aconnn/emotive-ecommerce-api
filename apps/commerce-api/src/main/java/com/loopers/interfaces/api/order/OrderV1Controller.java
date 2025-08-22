package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderAppService;
import com.loopers.application.order.PaymentStatusResult;
import com.loopers.domain.order.Order;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderAppService orderAppService;

    @PostMapping("")
    @Override
    public ApiResponse<OrderV1Dto.CreateResponse> create(
        @RequestBody OrderV1Dto.CreateRequest createRequest) {

        Order order = orderAppService.order(createRequest.toCommand());

        return ApiResponse.success(OrderV1Dto.CreateResponse.from(order));
    }

    @PostMapping("/callback")
    @Override
    public ApiResponse<String> callback(
            @RequestBody String payload
    ) {
        return ApiResponse.success("success");
    }

    @GetMapping("/{orderId}/payment-status")
    @Override
    public ApiResponse<OrderV1Dto.PaymentStatusResponse> getPaymentStatus(
            @PathVariable(value = "orderId") Long orderId) {

        PaymentStatusResult orderPaymentStatus = orderAppService.getOrderPaymentStatus(orderId);

        return ApiResponse.success(OrderV1Dto.PaymentStatusResponse.from(orderPaymentStatus));
    }
}
