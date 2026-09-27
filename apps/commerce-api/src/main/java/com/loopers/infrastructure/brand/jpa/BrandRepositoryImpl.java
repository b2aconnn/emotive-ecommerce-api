package com.loopers.infrastructure.brand.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.QBrand;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Component
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;
    private final JPAQueryFactory queryFactory;

    public BrandRepositoryImpl(BrandJpaRepository brandJpaRepository, EntityManager em) {
        this.brandJpaRepository = brandJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(brand);
    }

    @Override
    public List<Brand> findAll(String searchKeyword, Integer offset, Integer size) {
        return queryFactory
                .select(QBrand.brand)
                .from(QBrand.brand)
                .where(searchKeywordContains(searchKeyword))
                .orderBy(QBrand.brand.id.asc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    private BooleanExpression searchKeywordContains(String searchKeyword) {
        return StringUtils.hasText(searchKeyword) ? QBrand.brand.name.containsIgnoreCase(searchKeyword) : null;
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return brandJpaRepository.findById(id);
    }
}
