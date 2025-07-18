package com.loopers.domain.user.dto.data;

import com.loopers.domain.user.type.GenderType;
import jakarta.validation.constraints.NotNull;

public record UserCreateData(
    String userId,
    String name,
    String email,
    String birthDateString,
    @NotNull(message = "성별은 필수입니다.")
    GenderType gender
) {}
