package com.loopers.infrastructure.order.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.QOrder;
import com.loopers.domain.order.dto.result.UserOrderCountResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Component
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, EntityManager em) {
        this.orderJpaRepository = orderJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public List<Order> findAllByUserId(Long userId) {
        return queryFactory
                .select(QOrder.order)
                .from(QOrder.order)
                .where(QOrder.order.userId.eq(userId))
                .orderBy(QOrder.order.createdAt.desc())
                .fetch();
    }

    @Override
    public List<UserOrderCountResult> countCompletedOrdersGroupByUser(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive) {
        return queryFactory
                .select(Projections.constructor(UserOrderCountResult.class, QOrder.order.userId, QOrder.order.count()))
                .from(QOrder.order)
                .where(
                        QOrder.order.status.eq(OrderStatus.COMPLETED),
                        QOrder.order.createdAt.goe(periodStartInclusive),
                        QOrder.order.createdAt.lt(periodEndExclusive))
                .groupBy(QOrder.order.userId)
                .fetch();
    }
}
