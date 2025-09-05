package com.loopers.application.product;

import com.loopers.application.product.event.model.ProductViewedEvent;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductAppService {

    private final ProductRepository productRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductResult getProduct(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        ProductResult productResult = productOptional.map(ProductResult::from).orElse(null);

        if (productResult != null) {
            applicationEventPublisher.publishEvent(new ProductViewedEvent(productResult.id()));
        }

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
