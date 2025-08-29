package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static jakarta.persistence.EnumType.STRING;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "coupon")
@Entity
public class Coupon extends BaseEntity {

    private Long userId;

    @Enumerated(STRING)
    private CouponType type;

    private BigDecimal discountValue;

    private Boolean isUsed;

    private ZonedDateTime expirationDate;

    private Coupon(CouponCreateInfo createInfo) {
        this.userId = createInfo.userId();
        this.type = createInfo.type();
        this.isUsed = false;
        this.expirationDate = createInfo.expirationDate();
    }

    public static Coupon create(CouponCreateInfo createInfo) {
        validateRequiredCouponInfo(createInfo);
        return new Coupon(createInfo);
    }

    private static void validateRequiredCouponInfo(CouponCreateInfo createInfo) {
        requireNonNull(createInfo);
        requireNonNull(createInfo.userId());
        requireNonNull(createInfo.type());
        requireNonNull(createInfo.expirationDate());
    }

    public Long calculateDiscount(Long amount) {
        if (this.type == CouponType.FIXED_AMOUNT) {
            return this.discountValue.longValue();
        } else if (this.type == CouponType.PERCENTAGE) {
            BigDecimal amountDecimal = BigDecimal.valueOf(amount);
            return amountDecimal.multiply(this.discountValue).longValue();
        } else {
            throw new IllegalArgumentException("Unknown coupon type");
        }
    }

    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        if (expirationDate.isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }

        this.isUsed = true;
    }
}
