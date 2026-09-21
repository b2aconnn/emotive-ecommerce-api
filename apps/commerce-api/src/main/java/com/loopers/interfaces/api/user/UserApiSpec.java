package com.loopers.interfaces.api.user;

import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.UserCreateResponse;
import com.loopers.interfaces.api.user.dto.UserMyInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User API", description = "User API 입니다.")
public interface UserApiSpec {
    @Operation(
        summary = "회원 가입",
        description = "유저 회원 가입을 합니다."
    )
    ApiResponse<UserCreateResponse> create(
        @Schema(name = "유저 정보", description = "유저 정보")
        UserCreateInfo memberCreateRequest
    );

    @Operation(
        summary = "내 정보 조회",
        description = "ID로 내 정보를 조회합니다."
    )
    ApiResponse<UserMyInfoResponse> get(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)")
            Long userId
    );
}
