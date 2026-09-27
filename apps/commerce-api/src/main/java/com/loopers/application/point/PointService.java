package com.loopers.application.point;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.application.point.dto.PointResult;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(rollbackFor = Exception.class)
    public PointResult charge(Long userId, Long amount) {
        Point point = pointRepository.findByUserId(userId).orElse(Point.create(userId));

        point.charge(amount);

        return PointResult.from(point);
    }

    public PointResult get(Long userId) {
        Point point = pointRepository
                .findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 포인트 정보를 찾을 수 없습니다."));

        return PointResult.from(point);
    }
}
