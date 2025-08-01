package com.loopers.infrastructure.product.jpa;

import com.loopers.application.product.ProductsCond;
import com.loopers.application.product.ProductsSortType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.loopers.domain.product.QProduct.product;
import static org.springframework.util.StringUtils.hasText;

@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    private JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository,
                                EntityManager em) {
        this.productJpaRepository = productJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findAll(ProductsCond condition) {
        return queryFactory.select(product)
                .from(product)
                .where(searchKeywordContains(condition.searchKeyword()))
                .orderBy(createOrderSpecifiers(condition.sortBy()))
                .offset(condition.pageCond().offset())
                .limit(condition.pageCond().count())
                .fetch();
    }

    private BooleanExpression searchKeywordContains(String searchKeyword) {
        return hasText(searchKeyword) ? product.name.containsIgnoreCase(searchKeyword) : null;
    }

    public OrderSpecifier<?> createOrderSpecifiers(ProductsSortType sortType) {
        sortType = Objects.requireNonNullElse(sortType, ProductsSortType.LASTEST);

        return switch (sortType) {
            case LASTEST -> product.createdAt.desc();
            case PRICE_ASC -> product.price.asc();
            case LIKES_DESC -> product.productLikeCount.likeCount.desc();
        };
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }
}
