package com.loopers.infrastructure.product.jpa;

import static com.loopers.domain.brand.QBrand.brand;
import static com.loopers.domain.product.QProduct.product;
import static com.loopers.domain.product.QProductStock.productStock;
import static com.loopers.domain.productlike.QProductLike.productLike;
import static com.loopers.domain.productlike.QProductLikeCount.productLikeCount;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Component;

import com.loopers.application.product.dto.ProductsCondition;
import com.loopers.application.product.dto.ProductsSortType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Component
public class ProductRepositoryImpl implements ProductRepository {

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
                .select(product)
                .from(product)
                .leftJoin(product.brand, brand)
                .fetchJoin()
                .where(brandIdEq(condition.brandId()), searchKeywordContains(condition.searchKeyword()))
                .orderBy(createOrderSpecifiers(condition.sortBy()))
                .offset(0)
                .limit(20)
                .fetch();
    }

    private BooleanExpression searchKeywordContains(String searchKeyword) {
        return hasText(searchKeyword) ? product.name.containsIgnoreCase(searchKeyword) : null;
    }

    private BooleanExpression brandIdEq(Long brandId) {
        return nonNull(brandId) ? brand.id.eq(brandId) : null;
    }

    public OrderSpecifier<?> createOrderSpecifiers(ProductsSortType sortType) {
        sortType = requireNonNullElse(sortType, ProductsSortType.LASTEST);

        return switch (sortType) {
            case LASTEST -> product.createdAt.desc();
            case PRICE_ASC -> product.price.asc();
            case LIKES_DESC -> product.productLikeCount.likeCount.desc();
        };
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .select(product)
                .from(product)
                .leftJoin(product.brand, brand)
                .fetchJoin()
                .leftJoin(product.productLikeCount, productLikeCount)
                .fetchJoin()
                .leftJoin(product.productStock, productStock)
                .fetchJoin()
                .where(product.id.eq(id))
                .fetchOne());
    }

    public Optional<List<Product>> findByIdInWithStock(List<Long> ids) {
        return Optional.ofNullable(queryFactory
                .select(product)
                .from(product)
                .leftJoin(product.productStock, productStock)
                .fetchJoin()
                .where(product.id.in(ids))
                .fetch());
    }

    @Override
    public List<Product> findLikedByUserId(Long userId) {
        return queryFactory
                .select(product)
                .from(productLike)
                .join(productLike.product, product)
                .leftJoin(product.brand, brand)
                .fetchJoin()
                .where(productLike.user.id.eq(userId))
                .orderBy(productLike.createdAt.desc())
                .fetch();
    }
}
