package com.loopers.domain.product;

import com.loopers.domain.product.dto.command.ProductQuantityCommand;
import com.loopers.domain.product.dto.result.ProductQuantityResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> findProducts(List<Long> ids) {
        List<Product> products = productRepository.findByIds(ids)
                .orElse(List.of());

        if (products.size() != ids.size()) {
            throw new IllegalStateException("존재하지 않는 상품이 있습니다.");
        }

        return products;
    }

    private Map<Long, Product> getProductMap(List<Long> productIds) {
        return findProducts(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    public List<ProductQuantityResult> getProductWithStocks(List<ProductQuantityCommand> command) {
        List<Long> productIds = command.stream()
                .map(ProductQuantityCommand::productId).toList();

        Map<Long, Product> productMap = getProductMap(productIds);

        return command.stream()
                .map(e -> resolveProductWithStock(productMap, e.productId(), e.quantity())).toList();
    }

    private static ProductQuantityResult resolveProductWithStock(Map<Long, Product> productMap, Long productId, Long quantity) {
        Product product = productMap.get(productId);
        if (product == null) {
            throw new EntityNotFoundException("상품이 존재하지 않습니다.");
        }
        return new ProductQuantityResult(product, quantity);
    }

}
