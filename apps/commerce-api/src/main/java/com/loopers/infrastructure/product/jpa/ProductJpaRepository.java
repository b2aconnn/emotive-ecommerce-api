package com.loopers.infrastructure.product.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.product.Product;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);

    Optional<List<Product>> findByIdIn(List<Long> ids);
}
