package com.loopers.application.product;

import com.loopers.application.product.dto.ProductResult;
import com.loopers.application.product.dto.ProductsCondition;
import com.loopers.application.product.dto.ProductsResult;
import com.loopers.application.product.event.model.ProductViewedEvent;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Cacheable(value = "products", key = "#id")
    public ProductResult getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("[productId = " + id + "] 상품을 찾을 수 없습니다."));

        ProductResult productResult = ProductResult.from(product);
        applicationEventPublisher.publishEvent(new ProductViewedEvent(productResult.id()));

        return productResult;
    }

    @Cacheable(
            value = "products",
            key = "'offset:' + #productsCondition.offset() + ':size:' + #productsCondition.size()",
            condition = "#productsCondition.offset() == 0 || #productsCondition.offset() == 20 || #productsCondition.offset() == 40"
    )
    public List<ProductsResult> getAll(ProductsCondition productsCondition) {
        List<Product> products = productRepository.findAll(productsCondition);
        return products.stream()
                .map(ProductsResult::from)
                .toList();
    }
}
