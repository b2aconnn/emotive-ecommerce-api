package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderAppService;
import com.loopers.application.order.dto.OrderStatusResult;
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

    @GetMapping("/{orderId}/status")
    @Override
    public ApiResponse<OrderV1Dto.StatusResponse> getStatus(
            @PathVariable(value = "orderId") Long orderId) {

        OrderStatusResult orderPaymentStatus = orderAppService.getOrderStatus(orderId);

        return ApiResponse.success(OrderV1Dto.StatusResponse.from(orderPaymentStatus));
    }
}
