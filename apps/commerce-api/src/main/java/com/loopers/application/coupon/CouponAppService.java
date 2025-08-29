package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponAppService {

    private final CouponRepository couponRepository;

    @Transactional
    public void useCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new IllegalStateException("쿠폰 정보가 없습니다."));

        coupon.use();
    }

    @Transactional
    public void restoreCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new IllegalStateException("쿠폰 정보가 없습니다."));

        coupon.restore();
    }
}
