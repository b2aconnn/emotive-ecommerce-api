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

    @Builder
    private Point(String userId, Integer amount) {
        validateChargeAmount(amount);

        this.userId = userId;
        this.amount = amount;
    }

    private static void validateChargeAmount(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("0 이하의 포인트는 충전할 수 없습니다.");
        }
    }

    public static Point create(String userId, Integer amount) {
        return Point.builder()
                .userId(userId)
                .amount(amount)
                .build();
    }


    public void charge(Integer amount) {
        validateChargeAmount(amount);
        this.amount += amount;
    }
}
