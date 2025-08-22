package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface StockJpaRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findById(Long id);
    Optional<List<ProductStock>> findByIdIn(List<Long> ids);

    @Lock(PESSIMISTIC_WRITE)
    @Query("select p from ProductStock p where p.product.id in :ids")
    Optional<List<ProductStock>> findByProductIdsWithLock(List<Long> ids);
}
