package com.loopers.interfaces.api.user.dto;

import com.loopers.application.user.dto.UserResult;

import java.time.LocalDate;

public record UserMyInfoResponse(Long id,
                             String name,
                             String email,
                             LocalDate birthDate) {
    public static UserMyInfoResponse from(UserResult info) {
        return new UserMyInfoResponse(
                info.id(),
                info.name(),
                info.email(),
                info.birthDate()
        );
    }
}
