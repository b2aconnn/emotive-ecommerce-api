package com.loopers.infrastructure.coupon.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.coupon.Coupon;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByIdAndUserId(Long id, Long userId);
}
