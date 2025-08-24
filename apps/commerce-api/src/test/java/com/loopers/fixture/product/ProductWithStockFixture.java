package com.loopers.fixture.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;

public interface ProductWithStockFixture {
    Product save(Brand brand, Long productPrice, Long stockQuantity);
}
