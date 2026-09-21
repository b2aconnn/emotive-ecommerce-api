package com.loopers.domain.productlike.dto.result;

/**
 * 상품별 현재 좋아요 수.
 *
 * @param productId 상품 PK
 * @param likeCount 현재 좋아요 수. 좋아요 수 행이 존재하지 않는 상품은 결과에 포함되지 않는다.
 */
public record ProductLikeCountResult(
        Long productId,
        Long likeCount
) {}
