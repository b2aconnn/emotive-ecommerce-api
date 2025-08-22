package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.PGService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OrderAppService {

    private final UserService userService;

    private final OrderService orderService;

    private final OrderCalculator orderCalculator;

    private final ProductStockService productStockService;

    private final PointRepository pointRepository;

    private final PGService PGService;

    private final OrderRepository orderRepository;

    /**
     * 주문 생성
     * 재고 조회 및 예약
     * 포인트 조회 및 사용
     * 총 가격 계산
     * 결제 진행
     * 재고 차감
     * 주문 완료
     */
    @Transactional
    public Order order(OrderCreateCommand createCommand) {
        userService.existsById(createCommand.userId());

        Order saveOrder = orderService.createOrder(createCommand);
        List<OrderItem> orderItems = saveOrder.getOrderItems();

        Map<Product, ProductStock> productStocksMap = productStockService.reserveProducts(orderItems);

        Point point = pointRepository.findByUserId(createCommand.userId())
                .orElseThrow(() -> new IllegalStateException("포인트가 부족합니다."));
        point.use(createCommand.availablePoints());

        Long totalAmount = orderCalculator.calculateTotalAmount(orderItems, createCommand.availablePoints());
        saveOrder.updateTotalAmount(totalAmount);

        String callbackUrl = "http://localhost:8080/api/v1/orders/callback";
        PGService.requestPayment(saveOrder.getId(), createCommand.toPaymentRequest(totalAmount, callbackUrl));

        productStockService.deductStocks(orderItems, productStocksMap);

        saveOrder.pending();

        return saveOrder;
    }

    public PaymentStatusResult getOrderPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        PaymentStatusResult paymentStatus = PGService.getPaymentStatus(order.getPaymentId());

        return paymentStatus;
    }
}
