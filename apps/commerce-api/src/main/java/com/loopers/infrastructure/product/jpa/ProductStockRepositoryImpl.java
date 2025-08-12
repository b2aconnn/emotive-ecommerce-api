package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final StockJpaRepository stockJpaRepository;

    private JPAQueryFactory queryFactory;

    public ProductStockRepositoryImpl(StockJpaRepository stockJpaRepository,
                                      EntityManager em) {
        this.stockJpaRepository = stockJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public ProductStock save(ProductStock productStock) {
        return stockJpaRepository.save(productStock);
    }
    @Override
    public Optional<ProductStock> findById(Long id) {
        return stockJpaRepository.findById(id);
    }

    @Override
    public List<ProductStock> findStocks(List<Long> ids) {
        return stockJpaRepository.findByIdIn(ids)
                .orElse(List.of());
    }
}
