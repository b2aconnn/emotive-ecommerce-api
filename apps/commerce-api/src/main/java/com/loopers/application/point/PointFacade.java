package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;

    public PointInfo charge(String userId, Integer amount) {
        try {
            Point point = pointService.charge(userId, amount);
            return PointInfo.from(point);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public PointInfo get(String userId) {
        try {
            Point point = pointService.get(userId);
            return PointInfo.from(point);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
