package com.loopers.infrastructure.order.jpa;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.dto.result.UserOrderCountResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.loopers.domain.order.OrderStatus.COMPLETED;
import static com.loopers.domain.order.QOrder.order;

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
        return queryFactory.select(order)
                .from(order)
                .where(order.userId.eq(userId))
                .orderBy(order.createdAt.desc())
                .fetch();
    }

    @Override
    public List<UserOrderCountResult> countCompletedOrdersGroupByUser(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive) {
        return queryFactory
                .select(Projections.constructor(UserOrderCountResult.class, order.userId, order.count()))
                .from(order)
                .where(
                        order.status.eq(COMPLETED),
                        order.createdAt.goe(periodStartInclusive),
                        order.createdAt.lt(periodEndExclusive)
                )
                .groupBy(order.userId)
                .fetch();
    }
}
