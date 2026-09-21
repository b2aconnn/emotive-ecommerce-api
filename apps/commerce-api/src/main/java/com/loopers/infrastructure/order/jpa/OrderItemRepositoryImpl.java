package com.loopers.infrastructure.order.jpa;

import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.dto.result.UserOrderedProductResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static com.loopers.domain.order.OrderStatus.COMPLETED;
import static com.loopers.domain.order.QOrder.order;
import static com.loopers.domain.order.QOrderItem.orderItem;

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
                        UserOrderedProductResult.class, order.userId, orderItem.product.id))
                .distinct()
                .from(orderItem)
                .join(orderItem.order, order)
                .where(
                        order.status.eq(COMPLETED),
                        // 기간은 주문 상품이 아니라 주문(order)의 생성 시각 기준이다.
                        order.createdAt.goe(periodStartInclusive),
                        order.createdAt.lt(periodEndExclusive)
                )
                .fetch();
    }
}
