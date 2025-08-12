package com.loopers.fixture.infra.fixturemonkey;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStock;
import com.loopers.domain.product.ProductStockRepository;
import com.loopers.fixture.product.ProductWithStockFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
public class ProductFixtureMonkey implements ProductWithStockFixture {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository productStockRepository;

    private List<Product> createProduct(Brand brand, Long productPrice, int count) {
        return FixtureMonkeyFactory.create().giveMeBuilder(Product.class)
                .set("brand", brand)
                .set("price", productPrice)
                .set("id", 0L)
                .set("productStock", ProductStock.create(null, 1))
                .sampleList(count);
    }

    private Product createProduct(Brand brand, Long productPrice) {
        return Objects.requireNonNull(createProduct(brand, productPrice, 1)).get(0);
    }

    @Transactional
    @Override
    public Product save(Brand brand, Long productPrice, Integer stockQuantity) {
        Product saveProduct = productRepository.save(createProduct(brand, productPrice));
        ProductStock saveProductStock = productStockRepository.save(ProductStock.create(saveProduct, stockQuantity));
        saveProduct.setProductStock(saveProductStock);
        return saveProduct;
    }
}
