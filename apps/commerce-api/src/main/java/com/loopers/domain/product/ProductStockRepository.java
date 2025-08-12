package com.loopers.domain.product;


import java.util.List;
import java.util.Optional;

public interface ProductStockRepository {
    ProductStock save(ProductStock brand);
    Optional<ProductStock> findById(Long id);

    List<ProductStock> findStocks(List<Long> ids);
}
