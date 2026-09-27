package com.loopers.infrastructure.order.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.order.OrderItem;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {}
