package com.loopers.domain.order;

import com.loopers.domain.order.dto.result.UserOrderedProductResult;

import java.time.ZonedDateTime;
import java.util.List;

public interface OrderItemRepository {
    List<OrderItem> saveAll(List<OrderItem> orderItems);

    /**
     * 주어진 기간에 생성된 {@link OrderStatus#COMPLETED} 주문에 포함된 (사용자, 상품) 쌍을
     * 중복 제거해서 조회한다. 같은 사용자가 같은 상품을 여러 번 주문했어도 1건으로 나온다.
     * <p>
     * 구현체는 {@code orders x order_items} 조인 단일 쿼리로 계산해야 한다(사용자별 순회 금지).
     * 기간 경계는 주문(order)의 {@code createdAt} 기준 {@code periodStartInclusive <= createdAt < periodEndExclusive}이다.
     *
     * @param periodStartInclusive 기간 시작(포함). null이 아니어야 한다.
     * @param periodEndExclusive   기간 끝(미포함). null이 아니어야 한다.
     * @return 중복 제거된 (사용자, 상품) 쌍 목록. 없으면 빈 리스트(null 아님).
     */
    List<UserOrderedProductResult> findDistinctCompletedOrderedProductsByUser(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive);
}
