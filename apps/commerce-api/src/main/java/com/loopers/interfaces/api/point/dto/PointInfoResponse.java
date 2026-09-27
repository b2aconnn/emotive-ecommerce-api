package com.loopers.interfaces.api.point.dto;

import com.loopers.application.point.dto.PointResult;

public record PointInfoResponse(Long userId, Long amount) {
    public static PointInfoResponse from(PointResult result) {
        return new PointInfoResponse(result.userId(), result.amount());
    }
}
