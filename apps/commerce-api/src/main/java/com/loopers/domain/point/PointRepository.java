package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Point save(Point user);
    Optional<Point> findByUserId(Long id);
}
