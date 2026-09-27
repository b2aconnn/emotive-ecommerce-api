package com.loopers.domain.productlike.dto.result;

/**
 * "어떤 사용자가 어떤 상품에 좋아요를 눌렀다"를 나타내는 (사용자, 상품) 쌍.
 *
 * @param userId    사용자 내부 PK
 * @param productId 좋아요 대상 상품 PK
 */
public record UserLikedProductResult(Long userId, Long productId) {}
