package com.loopers.application.point;

import com.loopers.domain.point.Point;

public record PointInfo(Long id,
                        String userId,
                        Integer amount) {
    public static PointInfo from(Point point) {
        return new PointInfo(
            point.getId(),
            point.getUserId(),
            point.getAmount()
        );
    }
}
