package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;
import jakarta.validation.constraints.NotNull;

public class PointV1Dto {
    public record ChargeRequest(
        @NotNull(message = "충전할 포인트 금액을 입력해주세요.")
        Integer amount
    ) {}

    public record ChargeResponse(Integer amount) {
        public static ChargeResponse from(PointInfo info) {
            return new ChargeResponse(
                    info.amount()
            );
        }
    }

    public record InfoResponse(String userId,
                               Integer amount) {
        public static InfoResponse from(PointInfo info) {
            return new InfoResponse(
                    info.userId(),
                    info.amount()
            );
        }
    }
}
