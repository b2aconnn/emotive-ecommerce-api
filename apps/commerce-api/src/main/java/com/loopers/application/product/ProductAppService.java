package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductAppService {
    private final ProductRepository productRepository;

    public ProductInfo getProduct(Long id) {
        Optional<Product> brandOptional = productRepository.findById(id);
        return brandOptional.map(ProductInfo::from).orElse(null);
    }

    public List<ProductsInfo> getAll(ProductsCond productsCond) {
        List<Product> products = productRepository.findAll(productsCond);
        return products.stream()
                .map(ProductsInfo::from)
                .toList();
    }
}
