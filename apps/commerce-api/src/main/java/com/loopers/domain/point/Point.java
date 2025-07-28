package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "point")
@Entity
public class Point extends BaseEntity {

    private String userId;

    private Integer amount;

    private Point(String userId) {
        this.userId = userId;
        this.amount = 0;
    }

    public static Point create(String userId) {
        return new Point(userId);
    }

    private void validateChargeAmount(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("0 이하의 포인트는 충전할 수 없습니다.");
        }
    }


    public void charge(Integer amount) {
        validateChargeAmount(amount);
        this.amount += amount;
    }
}
