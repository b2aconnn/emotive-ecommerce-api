package com.loopers.domain.order;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.loopers.domain.order.dto.result.UserOrderCountResult;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAllByUserId(Long userId);

    /**
     * 주어진 기간에 생성된 {@link OrderStatus#COMPLETED} 주문을 사용자별로 집계한다.
     * <p>
     * 사용자 수만큼 쿼리를 반복하지 않도록 구현체는 {@code GROUP BY userId} 단일 쿼리로 계산해야 한다.
     * 기간 경계는 {@code periodStartInclusive <= createdAt < periodEndExclusive}로 판단한다.
     *
     * @param periodStartInclusive 기간 시작(포함). null이 아니어야 한다.
     * @param periodEndExclusive   기간 끝(미포함). null이 아니어야 한다.
     * @return 주문이 1건 이상인 사용자만 포함된 집계 결과. 없으면 빈 리스트(null 아님).
     */
    List<UserOrderCountResult> countCompletedOrdersGroupByUser(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive);
}
