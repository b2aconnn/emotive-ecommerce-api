package com.loopers.infrastructure.order.jpa;

import com.loopers.domain.order.Order;
import com.loopers.domain.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
