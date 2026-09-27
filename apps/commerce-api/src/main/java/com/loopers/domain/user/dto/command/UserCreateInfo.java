package com.loopers.domain.user.dto.command;

import jakarta.validation.constraints.NotNull;

import com.loopers.domain.user.type.GenderType;

public record UserCreateInfo(
        String name,
        String email,
        String birthDateString,
        @NotNull(message = "성별은 필수입니다.") GenderType gender) {}
