package com.loopers.domain.coupon;

import java.time.ZonedDateTime;

public record CouponCreateInfo(
        Long userId,
        CouponType type,
        ZonedDateTime expirationDate
) {}
