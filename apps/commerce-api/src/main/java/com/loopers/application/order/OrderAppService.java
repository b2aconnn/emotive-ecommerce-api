package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.payment.PaymentAppService;
import com.loopers.application.payment.dto.PaymentStatusResult;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.generator.PgIdGenerator;
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

    private final OrderRepository orderRepository;

    private final UserService userService;

    private final OrderService orderService;

    private final OrderCalculator orderCalculator;

    private final ProductStockService productStockService;

    private final PointRepository pointRepository;

    private final PaymentAppService paymentAppService;

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
        point.use(createCommand.usedPoints());

        Long totalAmount = orderCalculator.calculateTotalAmount(orderItems, createCommand.usedPoints());
        saveOrder.updateTotalAmount(totalAmount);

        paymentAppService.createPayment(createCommand.toPaymentCreateCommand(
                saveOrder.getId(),
                PgIdGenerator.generatePgOrderId(),
                totalAmount));

        productStockService.deductStocks(orderItems, productStocksMap);

        saveOrder.pending();

        return saveOrder;
    }

    public OrderStatusResult getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        PaymentStatusResult paymentStatusResult = paymentAppService.getPaymentStatus(order.getId());

        return new OrderStatusResult(order.getId(), paymentStatusResult.paymentStatus());
    }
}
