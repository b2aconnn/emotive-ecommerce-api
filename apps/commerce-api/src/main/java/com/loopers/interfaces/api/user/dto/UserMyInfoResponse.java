package com.loopers.interfaces.api.user.dto;

import java.time.LocalDate;

import com.loopers.application.user.dto.UserResult;

public record UserMyInfoResponse(Long id, String name, String email, LocalDate birthDate) {
    public static UserMyInfoResponse from(UserResult info) {
        return new UserMyInfoResponse(info.id(), info.name(), info.email(), info.birthDate());
    }
}
