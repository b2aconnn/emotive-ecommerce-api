package com.loopers.infrastructure.product.jpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.loopers.application.product.dto.ProductsCondition;
import com.loopers.application.product.dto.ProductsSortType;
import com.loopers.domain.brand.QBrand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.QProduct;
import com.loopers.domain.product.QProductStock;
import com.loopers.domain.productlike.QProductLike;
import com.loopers.domain.productlike.QProductLikeCount;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Component
public class ProductRepositoryImpl implements ProductRepository {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ProductJpaRepository productJpaRepository;
    private final JpaContext jpaContext;

    private JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository, EntityManager em, JpaContext jpaContext) {
        this.productJpaRepository = productJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
        this.jpaContext = jpaContext;
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findAll(ProductsCondition condition) {
        return queryFactory
                .select(QProduct.product)
                .from(QProduct.product)
                .leftJoin(QProduct.product.brand, QBrand.brand)
                .fetchJoin()
                .where(brandIdEq(condition.brandId()), searchKeywordContains(condition.searchKeyword()))
                .orderBy(createOrderSpecifiers(condition.sortBy()))
                .offset(0)
                .limit(DEFAULT_PAGE_SIZE)
                .fetch();
    }

    private BooleanExpression searchKeywordContains(String searchKeyword) {
        return StringUtils.hasText(searchKeyword) ? QProduct.product.name.containsIgnoreCase(searchKeyword) : null;
    }

    private BooleanExpression brandIdEq(Long brandId) {
        return Objects.nonNull(brandId) ? QBrand.brand.id.eq(brandId) : null;
    }

    public OrderSpecifier<?> createOrderSpecifiers(ProductsSortType sortType) {
        sortType = Objects.requireNonNullElse(sortType, ProductsSortType.LASTEST);

        return switch (sortType) {
            case LASTEST -> QProduct.product.createdAt.desc();
            case PRICE_ASC -> QProduct.product.price.asc();
            case LIKES_DESC -> QProduct.product.productLikeCount.likeCount.desc();
        };
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .select(QProduct.product)
                .from(QProduct.product)
                .leftJoin(QProduct.product.brand, QBrand.brand)
                .fetchJoin()
                .leftJoin(QProduct.product.productLikeCount, QProductLikeCount.productLikeCount)
                .fetchJoin()
                .leftJoin(QProduct.product.productStock, QProductStock.productStock)
                .fetchJoin()
                .where(QProduct.product.id.eq(id))
                .fetchOne());
    }

    public Optional<List<Product>> findByIdInWithStock(List<Long> ids) {
        return Optional.ofNullable(queryFactory
                .select(QProduct.product)
                .from(QProduct.product)
                .leftJoin(QProduct.product.productStock, QProductStock.productStock)
                .fetchJoin()
                .where(QProduct.product.id.in(ids))
                .fetch());
    }

    @Override
    public List<Product> findLikedByUserId(Long userId) {
        return queryFactory
                .select(QProduct.product)
                .from(QProductLike.productLike)
                .join(QProductLike.productLike.product, QProduct.product)
                .leftJoin(QProduct.product.brand, QBrand.brand)
                .fetchJoin()
                .where(QProductLike.productLike.user.id.eq(userId))
                .orderBy(QProductLike.productLike.createdAt.desc())
                .fetch();
    }
}
