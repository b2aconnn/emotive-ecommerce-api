package com.loopers.application.point;

import com.loopers.application.point.dto.PointInfo;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointAppService {

    private final UserRepository userRepository;

    private final PointRepository pointRepository;

    @Transactional(rollbackFor = Exception.class)
    public PointInfo charge(String userId, Long amount) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));

        Point point = pointRepository.findByUserId(user.getId())
                .orElse(Point.create(user.getId()));

        point.charge(amount);

        return PointInfo.from(point);
    }

    public PointInfo get(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));

        Optional<Point> pointOptional = pointRepository.findByUserId(user.getId());
        return pointOptional.map(PointInfo::from).orElse(null);
    }
}
