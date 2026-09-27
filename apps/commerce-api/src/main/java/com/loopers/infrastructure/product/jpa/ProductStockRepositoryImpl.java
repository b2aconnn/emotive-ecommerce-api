package com.loopers.infrastructure.product.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final ProductStockJpaRepository productStockJpaRepository;

    @Override
    public ProductStock save(ProductStock productStock) {
        return productStockJpaRepository.save(productStock);
    }

    @Override
    public Optional<List<ProductStock>> findByProductIdsWithStockLock(List<Long> productIds) {
        return productStockJpaRepository.findByProductIdsWithStockLock(productIds);
    }
}
