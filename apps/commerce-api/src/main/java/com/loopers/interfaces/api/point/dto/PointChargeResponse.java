package com.loopers.interfaces.api.point.dto;

import com.loopers.application.point.dto.PointResult;

public record PointChargeResponse(Long amount) {
    public static PointChargeResponse from(PointResult result) {
        return new PointChargeResponse(result.amount());
    }
}
