package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockJpaRepository extends JpaRepository<ProductStock, Long> {
}
