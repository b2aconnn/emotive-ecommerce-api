package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockJpaRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findById(Long id);
    Optional<List<ProductStock>> findByIdIn(List<Long> ids);
}
