package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
    Optional<List<Product>> findByIdIn(List<Long> ids);
}
