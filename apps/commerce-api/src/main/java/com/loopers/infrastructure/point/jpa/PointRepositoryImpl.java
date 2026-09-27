package com.loopers.infrastructure.point.jpa;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public Optional<Point> findByUserId(Long userId) {
        return pointJpaRepository.findByUserId(userId);
    }
}
