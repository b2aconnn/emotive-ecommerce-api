package com.loopers.infrastructure.order.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.order.Order;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {}
