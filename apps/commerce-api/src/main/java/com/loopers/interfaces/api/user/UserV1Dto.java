package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;

import java.time.LocalDate;

public class UserV1Dto {
    public record CreateResponse(String userId,
                                 String name,
                                 String email,
                                 LocalDate birthDate) {
        public static CreateResponse from(UserInfo info) {
            return new CreateResponse(
                info.userId(),
                info.name(),
                info.email(),
                info.birthDate()
            );
        }
    }

    public record MyInfoResponse(String userId,
                                 String name,
                                 String email,
                                 LocalDate birthDate) {
        public static MyInfoResponse from(UserInfo info) {
            return new MyInfoResponse(
                    info.userId(),
                    info.name(),
                    info.email(),
                    info.birthDate()
            );
        }
    }
}
