package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "point")
@Entity
public class Point extends BaseEntity {

    private Long userId;

    private Long balance;

    private Point(Long userId) {
        this.userId = userId;
        this.balance = 0L;
    }

    public static Point create(Long userId) {
        return new Point(userId);
    }

    public void charge(Long amount) {
        validateChargeAmount(amount);
        this.balance += amount;
    }

    private void validateChargeAmount(Long amount) {
        if (amount < 100) {
            throw new IllegalArgumentException("100 이상의 포인트를 충전할 수 있습니다.");
        }
    }

    public void use(Long amount) {
        validateUseAmount(amount);
        this.balance -= amount;
    }

    private void validateUseAmount(Long amount) {
        if (amount < 100) {
            throw new IllegalArgumentException("포인트는 100원 이상 사용할 수 있습니다.");
        }

        if (this.balance < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }
}
