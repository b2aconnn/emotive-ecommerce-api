package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderService;
import com.loopers.application.order.dto.OrderDetailResult;
import com.loopers.application.order.dto.OrderResult;
import com.loopers.application.order.dto.OrderStatusResult;
import com.loopers.domain.order.Order;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.order.dto.OrderCreateRequest;
import com.loopers.interfaces.api.order.dto.OrderCreateResponse;
import com.loopers.interfaces.api.order.dto.OrderDetailResponse;
import com.loopers.interfaces.api.order.dto.OrderStatusResponse;
import com.loopers.interfaces.api.order.dto.OrdersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController implements OrderApiSpec {

    private final OrderService orderService;

    @Override
    @PostMapping("")
    public ApiResponse<OrderCreateResponse> create(
        @RequestHeader("X-USER-ID") Long userId,
        @RequestBody OrderCreateRequest createRequest) {

        Order order = orderService.order(userId, createRequest.toCommand());

        return ApiResponse.success(OrderCreateResponse.from(order));
    }

    @Override
    @GetMapping("/{orderId}/status")
    public ApiResponse<OrderStatusResponse> getStatus(
            @PathVariable(value = "orderId") Long orderId) {

        OrderStatusResult orderPaymentStatus = orderService.getOrderStatus(orderId);

        return ApiResponse.success(OrderStatusResponse.from(orderPaymentStatus));
    }

    @Override
    @GetMapping("")
    public ApiResponse<List<OrdersResponse>> getOrders(
            @RequestHeader("X-USER-ID") Long userId) {

        List<OrderResult> orders = orderService.getOrders(userId);

        List<OrdersResponse> response = orders.stream()
                .map(OrdersResponse::from)
                .toList();
        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable(value = "orderId") Long orderId) {

        OrderDetailResult order = orderService.getOrder(userId, orderId);

        return ApiResponse.success(OrderDetailResponse.from(order));
    }
}
