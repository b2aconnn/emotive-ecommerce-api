package com.loopers.infrastructure.product.jpa;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.loopers.domain.product.ProductStock;

public interface ProductStockJpaRepository extends JpaRepository<ProductStock, Long> {
    @Lock(PESSIMISTIC_WRITE)
    @Query("select ps from ProductStock ps where ps.product.id in :productIds")
    Optional<List<ProductStock>> findByProductIdsWithStockLock(List<Long> productIds);
}
