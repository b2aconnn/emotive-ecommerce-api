package com.loopers.infrastructure.brand.jpa;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandJpaRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findById(Long brandId);
}
