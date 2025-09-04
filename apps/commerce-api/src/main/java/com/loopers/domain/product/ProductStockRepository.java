package com.loopers.domain.product;


import java.util.List;
import java.util.Optional;

public interface ProductStockRepository {
    ProductStock save(ProductStock productStock);

    Optional<List<ProductStock>> findByProductIdsWithStockLock(
            List<Long> productIds);
}
