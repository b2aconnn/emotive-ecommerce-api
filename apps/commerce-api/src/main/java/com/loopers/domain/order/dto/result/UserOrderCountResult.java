package com.loopers.domain.order.dto.result;

/**
 * 사용자별 주문 건수 집계 결과 한 행(GROUP BY userId).
 *
 * @param userId     사용자 내부 PK
 * @param orderCount 해당 사용자의 주문 건수
 */
public record UserOrderCountResult(
        Long userId,
        Long orderCount
) {}
