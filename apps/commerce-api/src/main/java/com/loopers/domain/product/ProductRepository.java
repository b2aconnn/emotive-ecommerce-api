package com.loopers.domain.product;


import com.loopers.application.product.ProductsCondition;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product brand);

    List<Product> findAll(ProductsCondition productsCondition);
    Optional<Product> findById(Long id);
    Optional<List<Product>> findByIdInWithStock(List<Long> ids);
}
