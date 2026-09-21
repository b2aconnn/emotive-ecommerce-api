package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.productlike.ProductLikeCount;
import com.loopers.domain.productlike.ProductLikeCountRepository;
import com.loopers.domain.productlike.dto.result.ProductLikeCountResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.loopers.domain.productlike.QProductLikeCount.productLikeCount;
import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class ProductLikeCountRepositoryImpl implements ProductLikeCountRepository {

    private final ProductLikeCountJpaRepository productLikeCountJpaRepository;
    private final JPAQueryFactory queryFactory;

    public ProductLikeCountRepositoryImpl(ProductLikeCountJpaRepository productLikeCountJpaRepository,
                                          EntityManager em) {
        this.productLikeCountJpaRepository = productLikeCountJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public ProductLikeCount save(ProductLikeCount productLikeCount) {
        return productLikeCountJpaRepository.save(productLikeCount);
    }

    @Override
    public Optional<ProductLikeCount> findByProductId(Long productId) {
        return productLikeCountJpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductLikeCountResult> findLikeCountsByProductIds(List<Long> productIds) {
        // 빈 목록으로 IN () 쿼리를 만들지 않는다.
        if (isEmpty(productIds)) {
            return emptyList();
        }

        return queryFactory
                .select(Projections.constructor(
                        ProductLikeCountResult.class,
                        productLikeCount.product.id,
                        // 엔티티상 likeCount는 Integer라 결과 record(Long)에 맞춰 캐스팅한다.
                        productLikeCount.likeCount.longValue()))
                .from(productLikeCount)
                .where(productLikeCount.product.id.in(productIds))
                .fetch();
    }
}
