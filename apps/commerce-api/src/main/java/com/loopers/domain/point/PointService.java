package com.loopers.domain.point;

import com.loopers.domain.order.Discount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.loopers.domain.order.DiscountType.POINT;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    public Discount createPointDiscount(Long userId, Long usedPoints) {
        if (usedPoints == null || usedPoints <= 0) {
            return new Discount(0L, POINT);
        }

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 존재하지 않습니다."));

        if (point.getBalance() < usedPoints) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }

        return new Discount(usedPoints, POINT);
    }

    public void deductPoints(Long userId, Long usedPoints) {
        if (usedPoints == null || usedPoints <= 0) {
            return;
        }

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 존재하지 않습니다."));

        point.use(usedPoints);
    }
}
