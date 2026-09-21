package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderDetailResult;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.application.order.dto.OrderResult;
import com.loopers.application.order.dto.OrderStatusResult;
import com.loopers.application.order.event.model.OrderCompletedEvent;
import com.loopers.application.order.event.model.OrderCreatedEvent;
import com.loopers.application.order.event.model.OrderCanceledEvent;
import com.loopers.domain.coupon.CouponRedemption;
import com.loopers.domain.order.Discount;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderProcessing;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductStockAllocation;
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
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderProcessing orderProcessing;

    private final ProductStockAllocation productStockAllocation;

    private final PointRepository pointRepository;

    private final CouponRedemption couponRedemption;

    private final ApplicationEventPublisher applicationEventPublisher;


    /**
     * ### 주문 프로세스 V2
     * 포인트, 쿠폰
     * 주문 생성 (주문 기본 정보(연락처, 배송지 등), 주문하는 상품 저장, 주문 상품 총 가격)
     * 재고 차감 (예약)
     */

    /**
     * ### 주문 프로세스 V1
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
    public Order order(Long userId, OrderCreateCommand createCommand) {
        List<Long> productIds = createCommand.items().stream()
                .map(OrderLineItem::productId).toList();
        Products products = productStockAllocation.reserveProducts(productIds);

        Order saveOrder = orderProcessing.saveOrder(userId, createCommand, products);

        Discount couponDiscount = couponRedemption.calculateDiscount(
                userId,
                createCommand.couponId(),
                saveOrder.getItemTotalAmount());

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "포인트 정보가 없습니다. userId: " + userId));
        point.use(createCommand.usedPoints());

        saveOrder.applyDiscount(List.of(couponDiscount));

        productStockAllocation.deductStocks(products, createCommand.items());

        saveOrder.pending();

        applicationEventPublisher.publishEvent(new OrderCreatedEvent(
                saveOrder.getId(),
                userId,
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

    public List<OrderResult> getOrders(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderResult::from)
                .toList();
    }

    public OrderDetailResult getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("[orderId = " + orderId + "] 주문을 찾을 수 없습니다."));

        if (!order.getUserId().equals(userId)) {
            throw new EntityNotFoundException("[orderId = " + orderId + "] 주문을 찾을 수 없습니다.");
        }

        return OrderDetailResult.from(order);
    }

    @Transactional
    public void cancelOrderWithRestoration(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        orderProcessing.restoreProductStocks(order.getOrderItems());

        Point point = pointRepository.findByUserId(order.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "포인트 정보가 없습니다. userId: " + order.getUserId()));
        point.restorePoint(order.getUsedPoints());

        order.cancel();

        applicationEventPublisher.publishEvent(new OrderCanceledEvent(
                order.getCouponId(),
                order.getUserId()));
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        order.complete();

        applicationEventPublisher.publishEvent(new OrderCompletedEvent(
                orderId,
                order.getOrderItems()
        ));
    }
}
