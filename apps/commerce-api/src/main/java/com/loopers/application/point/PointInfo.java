package com.loopers.application.point;

import com.loopers.domain.point.Point;

public record PointInfo(Long id,
                        Long userId,
                        Long amount) {
    public static PointInfo from(Point point) {
        return new PointInfo(
            point.getId(),
            point.getUserId(),
            point.getBalance()
        );
    }
}
