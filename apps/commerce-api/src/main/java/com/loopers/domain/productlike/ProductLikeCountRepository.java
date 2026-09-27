package com.loopers.domain.productlike;

import java.util.List;
import java.util.Optional;

import com.loopers.domain.productlike.dto.result.ProductLikeCountResult;

public interface ProductLikeCountRepository {
    ProductLikeCount save(ProductLikeCount productLikeCount);

    Optional<ProductLikeCount> findByProductId(Long productId);

    /**
     * 여러 상품의 현재 좋아요 수를 한 번에 조회한다(상품별 반복 조회 금지).
     *
     * @param productIds 조회할 상품 PK 목록. 비어 있으면 빈 리스트를 반환한다.
     * @return 좋아요 수 행이 존재하는 상품만 포함된 결과. 없으면 빈 리스트(null 아님).
     */
    List<ProductLikeCountResult> findLikeCountsByProductIds(List<Long> productIds);
}
