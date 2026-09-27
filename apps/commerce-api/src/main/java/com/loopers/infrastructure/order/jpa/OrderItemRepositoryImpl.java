package com.loopers.infrastructure.order.jpa;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.QOrder;
import com.loopers.domain.order.QOrderItem;
import com.loopers.domain.order.dto.result.UserOrderedProductResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Component
public class OrderItemRepositoryImpl implements OrderItemRepository {
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final JPAQueryFactory queryFactory;

    public OrderItemRepositoryImpl(OrderItemJpaRepository orderItemJpaRepository, EntityManager em) {
        this.orderItemJpaRepository = orderItemJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        return orderItemJpaRepository.saveAll(orderItems);
    }

    @Override
    public List<UserOrderedProductResult> findDistinctCompletedOrderedProductsByUser(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive) {
        return queryFactory
                .select(Projections.constructor(
                        UserOrderedProductResult.class, QOrder.order.userId, QOrderItem.orderItem.product.id))
                .distinct()
                .from(QOrderItem.orderItem)
                .join(QOrderItem.orderItem.order, QOrder.order)
                .where(
                        QOrder.order.status.eq(OrderStatus.COMPLETED),
                        // 기간은 주문 상품이 아니라 주문(QOrder.order)의 생성 시각 기준이다.
                        QOrder.order.createdAt.goe(periodStartInclusive),
                        QOrder.order.createdAt.lt(periodEndExclusive))
                .fetch();
    }
}
