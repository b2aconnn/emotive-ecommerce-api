package com.loopers.fixture.infra.fixturemonkey;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.fixture.point.PointFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class PointFixtureMonkey implements PointFixture {
    @Autowired
    private PointRepository pointRepository;

    private List<Point> create(Long userId, Long balance, int count) {
        return FixtureMonkeyFactory.create().giveMeBuilder(Point.class)
                .set("id", 0L)
                .set("userId", userId)
                .set("balance", balance)
                .sampleList(count);
    }

    private Point create(Long userId, Long balance) {
        return Objects.requireNonNull(create(userId, balance, 1)).get(0);
    }

    @Override
    public Point save(Long userId, Long balance) {
        Point point = create(userId, balance);
        return pointRepository.save(point);
    }
}
