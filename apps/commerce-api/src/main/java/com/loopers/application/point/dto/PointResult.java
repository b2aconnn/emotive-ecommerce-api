package com.loopers.application.point.dto;

import com.loopers.domain.point.Point;

public record PointResult(Long id, Long userId, Long amount) {
    public static PointResult from(Point point) {
        return new PointResult(point.getId(), point.getUserId(), point.getBalance());
    }
}
