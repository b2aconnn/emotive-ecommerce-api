package com.loopers.domain.point;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.loopers.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "point")
@Entity
public class Point extends BaseEntity {

    private static final long MIN_CHARGE_AMOUNT = 100L;
    private static final long MIN_USE_AMOUNT = 100L;

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
        if (amount < MIN_CHARGE_AMOUNT) {
            throw new IllegalArgumentException("100 이상의 포인트를 충전할 수 있습니다.");
        }
    }

    public void use(Long amount) {
        validateUseAmount(amount);
        this.balance -= amount;
    }

    private void validateUseAmount(Long useAmount) {
        if (useAmount < MIN_USE_AMOUNT) {
            throw new IllegalArgumentException("포인트는 100원 이상 사용할 수 있습니다.");
        }

        if (this.balance < useAmount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }

    public void restorePoint(Long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("복원할 포인트는 0 이상이어야 합니다.");
        }
        this.balance += amount;
    }
}
