package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

import com.loopers.application.product.dto.ProductsCondition;

public interface ProductRepository {
    Product save(Product brand);

    List<Product> findAll(ProductsCondition productsCondition);

    Optional<Product> findById(Long id);

    Optional<List<Product>> findByIdInWithStock(List<Long> ids);

    List<Product> findLikedByUserId(Long userId);
}
