package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.dto.PointChargeRequest;
import com.loopers.interfaces.api.point.dto.PointChargeResponse;
import com.loopers.interfaces.api.point.dto.PointInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Point API", description = "Point API 입니다.")
public interface PointApiSpec {
    @Operation(
        summary = "포인트 금액 충전",
        description = "ID로 포인트 금액를 충전합니다."
    )
    ApiResponse<PointChargeResponse> charge(
        @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)")
        Long userId,
        @Schema(name = "포인트 금액", description = "충전할 포인트 금액")
        PointChargeRequest chargeRequest
    );

    @Operation(
        summary = "포인트 정보 조회",
        description = "ID로 포인트 정보를 조회합니다."
    )
    ApiResponse<PointInfoResponse> get(
        @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)")
        Long userId
    );
}
