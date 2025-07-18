package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.dto.data.UserCreateData;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {
    private final UserFacade userFacade;

    @PostMapping("")
    @Override
    public ApiResponse<UserV1Dto.CreateResponse> create(
            @Valid @RequestBody UserCreateData memberCreateRequest) {
        UserInfo info = userFacade.create(memberCreateRequest);
        UserV1Dto.CreateResponse response = UserV1Dto.CreateResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("/{userId}")
    @Override
    public ApiResponse<UserV1Dto.MyInfoResponse> get(
            @PathVariable(value = "userId") String userId
    ) {
        UserInfo info = userFacade.get(userId);
        if (info == null) {
            throw new EntityNotFoundException("해당 사용자를 찾을 수 없습니다.");
        }
        UserV1Dto.MyInfoResponse response = UserV1Dto.MyInfoResponse.from(info);
        return ApiResponse.success(response);
    }
}
