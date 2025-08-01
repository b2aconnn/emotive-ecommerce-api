package com.loopers.interfaces.api.user;

import com.loopers.domain.user.dto.command.UserCreateCommand;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "User API 입니다.")
public interface UserV1ApiSpec {
    @Operation(
        summary = "회원 가입",
        description = "유저 회원 가입을 합니다."
    )
    ApiResponse<UserV1Dto.CreateResponse> create(
        @Schema(name = "유저 정보", description = "유저 정보")
        UserCreateCommand memberCreateRequest
    );

    @Operation(
        summary = "내 정보 조회",
        description = "ID로 내 정보를 조회합니다."
    )
    ApiResponse<UserV1Dto.MyInfoResponse> get(
            @Schema(name = "나의 유저 ID", description = "조회할 나의 유저 ID")
            String userId
    );
}
