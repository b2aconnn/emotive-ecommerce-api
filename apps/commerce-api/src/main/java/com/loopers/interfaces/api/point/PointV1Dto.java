package com.loopers.interfaces.api.point;

import com.loopers.application.point.dto.PointInfo;
import jakarta.validation.constraints.NotNull;

public class PointV1Dto {
    public record ChargeRequest(
        @NotNull(message = "충전할 포인트 금액을 입력해주세요.")
        Long amount
    ) {}

    public record ChargeResponse(Long amount) {
        public static ChargeResponse from(PointInfo info) {
            return new ChargeResponse(
                    info.amount()
            );
        }
    }

    public record InfoResponse(Long userId,
                               Long amount) {
        public static InfoResponse from(PointInfo info) {
            return new InfoResponse(
                    info.userId(),
                    info.amount()
            );
        }
    }
}
