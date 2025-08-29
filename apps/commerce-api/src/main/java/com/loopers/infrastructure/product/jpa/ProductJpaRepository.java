package com.loopers.infrastructure.product.jpa;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
    Optional<List<Product>> findByIdIn(List<Long> ids);

    @Lock(PESSIMISTIC_WRITE)
    @Query("select p from Product p join fetch p.productStock where p.id in :ids")
    Optional<List<ProductStock>> findByIdsWithStockLock(List<Long> ids);
}
