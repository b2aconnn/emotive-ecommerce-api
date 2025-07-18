package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Component
public class PointService {
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    @Transactional(rollbackFor = Exception.class)
    public Point charge(String userId, Integer amount) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));

        Point point = pointRepository.findByUserId(user.getUserId())
                .orElse(Point.create(user.getUserId(), amount));

        point.charge(amount);

        return point;
    }

    public Point get(String userId) {
        return pointRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));
    }
}
