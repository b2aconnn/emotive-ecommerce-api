package com.loopers.domain.coupon;

import com.loopers.domain.order.Discount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.loopers.domain.order.DiscountType.COUPON;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public Discount calculateDiscount(Long userId, Long couponId, Long orderAmount) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않는 쿠폰입니다."));

        Long discountAmount = coupon.calculateDiscount(orderAmount);

        return new Discount(discountAmount, COUPON);
    }

    public void useCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않는 쿠폰입니다."));

        coupon.use();

        couponRepository.save(coupon);
    }
}
