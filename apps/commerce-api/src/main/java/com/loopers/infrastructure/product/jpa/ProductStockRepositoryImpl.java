package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final StockJpaRepository stockJpaRepository;

    @Override
    public ProductStock save(ProductStock productStock) {
        return stockJpaRepository.save(productStock);
    }
}
