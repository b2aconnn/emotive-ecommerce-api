package com.loopers.interfaces.api.user;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.user.UserService;
import com.loopers.application.user.dto.UserResult;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.UserCreateResponse;
import com.loopers.interfaces.api.user.dto.UserMyInfoResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserApiSpec {
    private final UserService userService;

    @Override
    @PostMapping("")
    public ApiResponse<UserCreateResponse> create(@Valid @RequestBody UserCreateInfo memberCreateRequest) {
        UserResult info = userService.create(memberCreateRequest);
        UserCreateResponse response = UserCreateResponse.from(info);
        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<UserMyInfoResponse> get(@RequestHeader("X-USER-ID") Long userId) {
        UserResult info = userService.get(userId);
        UserMyInfoResponse response = UserMyInfoResponse.from(info);
        return ApiResponse.success(response);
    }
}
