package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.application.order.dto.OrderStatusResult;
import com.loopers.application.order.event.model.CreateOrderEvent;
import com.loopers.application.order.event.model.OrderCancelEvent;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Discount;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.Products;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderAppService {

    private final OrderRepository orderRepository;

    private final OrderService orderService;

    private final ProductService productService;

    private final PointRepository pointRepository;

    private final CouponService couponService;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * ### 주문 프로세스
     * 상품 재고 예약
     * 주문 생성
     * 쿠폰 조회
     * 포인트 조회 및 사용
     * 할인 적용
     * 결제 진행
     * 재고 차감
     * 주문 결제 대기
     */
    @Transactional
    public Order order(OrderCreateCommand createCommand) {
        List<Long> productIds = createCommand.items().stream()
                .map(OrderLineItem::productId).toList();
        Products products = productService.reserveProducts(productIds);

        Order saveOrder = orderService.saveOrder(createCommand, products);

        Discount couponDiscount = couponService.calculateDiscount(
                createCommand.userId(),
                createCommand.couponId(),
                saveOrder.getItemTotalAmount());

        Point point = pointRepository.findByUserId(createCommand.userId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "포인트 정보가 없습니다. userId: " + createCommand.userId()));
        point.use(createCommand.usedPoints());

        saveOrder.applyDiscount(List.of(couponDiscount));

        productService.deductStocks(products, createCommand.items());

        saveOrder.pending();

        applicationEventPublisher.publishEvent(new CreateOrderEvent(
                saveOrder.getId(),
                createCommand.userId(),
                createCommand.couponId(),
                saveOrder.getTotalAmount(),
                createCommand.paymentMethod(),
                createCommand.cardType(),
                createCommand.cardNo()
        ));

        return saveOrder;
    }

    public OrderStatusResult getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        return new OrderStatusResult(order.getId(), order.getStatus());
    }

    @Transactional
    public void cancelOrderWithRestoration(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        orderService.restoreProductStocks(order.getOrderItems());

        Point point = pointRepository.findByUserId(order.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "포인트 정보가 없습니다. userId: " + order.getUserId()));
        point.restorePoint(order.getUsedPoints());

        order.cancel();

        applicationEventPublisher.publishEvent(new OrderCancelEvent(
                order.getCouponId(),
                order.getUserId()));
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        order.complete();
    }
}
