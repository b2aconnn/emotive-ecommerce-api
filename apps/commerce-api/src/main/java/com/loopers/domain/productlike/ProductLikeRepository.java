package com.loopers.domain.productlike;

import com.loopers.domain.productlike.dto.result.UserLikedProductResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);
    void delete(ProductLike productLike);

    boolean existsUserLikedProduct(Long userId, Long productId);
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * 주어진 기간에 눌린 좋아요의 (사용자, 상품) 쌍을 조회한다.
     * <p>
     * 조회 시점에 <b>남아 있는</b> 좋아요만 반환한다 — 기간 중 눌렀다가 취소한 좋아요는 포함되지 않는다
     * (집계 시점의 현재 상태 스냅샷 기준). 기간 경계는 좋아요의 {@code createdAt} 기준
     * {@code periodStartInclusive <= createdAt < periodEndExclusive}이다.
     *
     * @param periodStartInclusive 기간 시작(포함). null이 아니어야 한다.
     * @param periodEndExclusive   기간 끝(미포함). null이 아니어야 한다.
     * @return (사용자, 상품) 쌍 목록. 없으면 빈 리스트(null 아님).
     */
    List<UserLikedProductResult> findLikedProductsByUserInPeriod(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive);
}
