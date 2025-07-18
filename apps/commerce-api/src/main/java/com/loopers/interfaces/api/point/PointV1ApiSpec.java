package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Point V1 API", description = "Point API 입니다.")
public interface PointV1ApiSpec {
    @Operation(
        summary = "포인트 금액 충전",
        description = "ID로 포인트 금액를 충전합니다."
    )
    ApiResponse<PointV1Dto.ChargeResponse> charge(
        @Schema(name = "포인트 금액", description = "충전할 포인트 금액")
        PointV1Dto.ChargeRequest chargeRequest
    );

    @Operation(
        summary = "포인트 정보 조회",
        description = "ID로 포인트 정보를 조회합니다."
    )
    ApiResponse<PointV1Dto.InfoResponse> get(
            @Schema(name = "유저 ID", description = "조회할 유저 ID")
            String userId
    );
}
