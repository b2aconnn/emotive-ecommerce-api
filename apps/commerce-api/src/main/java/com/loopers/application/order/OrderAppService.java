package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.application.order.dto.OrderStatusResult;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Discount;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.Products;
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

    private final PointService pointService;

    private final CouponService couponService;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 상품 재고 예약
     * 주문 생성
     * 포인트 조회
     * 쿠폰 조회
     * 할인 적용
     * 결제 진행
     * 재고 차감
     * 주문 결제 대기
     */
    /**
     * ### 주문 생성 후 처리해야 할 것 (이벤트 발행)
     * 포인트 사용 처리
     * 쿠폰 사용 처리
     * 결제 요청 처리
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

        Discount pointDiscount = pointService.createPointDiscount(
                createCommand.userId(),
                createCommand.usedPoints());

        saveOrder.applyDiscount(List.of(couponDiscount, pointDiscount));

        productService.deductStocks(products, createCommand.items());

        saveOrder.pending();

        return saveOrder;
    }

    public OrderStatusResult getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        return new OrderStatusResult(order.getId(), order.getStatus());
    }
}
