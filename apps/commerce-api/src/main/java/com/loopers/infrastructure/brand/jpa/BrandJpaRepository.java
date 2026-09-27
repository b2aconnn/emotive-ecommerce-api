package com.loopers.infrastructure.brand.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.brand.Brand;

public interface BrandJpaRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findById(Long brandId);
}
