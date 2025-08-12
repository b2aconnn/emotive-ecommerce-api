package com.loopers.domain.product;


import com.loopers.application.product.ProductsCond;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product brand);

    List<Product> findAll(ProductsCond productsCond);
    Optional<Product> findById(Long id);
    Optional<List<Product>> findByIds(List<Long> id);
}
