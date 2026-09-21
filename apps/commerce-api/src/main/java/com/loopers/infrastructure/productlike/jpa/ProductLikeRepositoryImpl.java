package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeRepository;
import com.loopers.domain.productlike.dto.result.UserLikedProductResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.loopers.domain.productlike.QProductLike.productLike;

@Component
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;
    private final JPAQueryFactory queryFactory;

    public ProductLikeRepositoryImpl(ProductLikeJpaRepository productLikeJpaRepository, EntityManager em) {
        this.productLikeJpaRepository = productLikeJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public ProductLike save(ProductLike productLike) {
        return productLikeJpaRepository.save(productLike);
    }

    @Override
    public void delete(ProductLike productLike) {
        productLikeJpaRepository.delete(productLike);
    }

    @Override
    public boolean existsUserLikedProduct(Long userId, Long productId) {
        return productLikeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId) {
        return productLikeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<UserLikedProductResult> findLikedProductsByUserInPeriod(
            ZonedDateTime periodStartInclusive, ZonedDateTime periodEndExclusive) {
        // deletedAt 필터를 넣지 않는다. delete()가 하드 삭제라 "행 존재 = 현재 좋아요 유지"이므로,
        // 기간 조건만으로 "기간 중 눌렀고 집계 시점까지 살아있는 좋아요" 스냅샷이 그대로 나온다.
        return queryFactory
                .select(Projections.constructor(
                        UserLikedProductResult.class, productLike.user.id, productLike.product.id))
                .from(productLike)
                .where(
                        productLike.createdAt.goe(periodStartInclusive),
                        productLike.createdAt.lt(periodEndExclusive)
                )
                .fetch();
    }
}
