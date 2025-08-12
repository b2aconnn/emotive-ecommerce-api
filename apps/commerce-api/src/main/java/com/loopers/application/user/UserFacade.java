package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo create(UserCreateInfo userCreateInfo) {
        User user = userService.create(userCreateInfo);
        return UserInfo.from(user);
    }

    public UserInfo get(String userId) {
        try {
            User user = userService.get(userId);
            return UserInfo.from(user);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
