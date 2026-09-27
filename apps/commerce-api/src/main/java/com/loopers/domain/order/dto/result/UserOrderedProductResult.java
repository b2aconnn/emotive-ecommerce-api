package com.loopers.domain.order.dto.result;

/**
 * "어떤 사용자가 어떤 상품을 주문했다"를 나타내는 (사용자, 상품) 쌍. 중복은 제거되어 있다.
 *
 * @param userId    사용자 내부 PK
 * @param productId 주문에 포함된 상품 PK
 */
public record UserOrderedProductResult(Long userId, Long productId) {}
