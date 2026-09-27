package com.loopers.interfaces.api.point.dto;

import jakarta.validation.constraints.NotNull;

public record PointChargeRequest(
        @NotNull(message = "충전할 포인트 금액을 입력해주세요.") Long amount) {}
