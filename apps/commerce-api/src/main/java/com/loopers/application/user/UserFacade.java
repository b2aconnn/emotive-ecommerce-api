package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.dto.data.UserCreateCommand;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo create(UserCreateCommand userCreateCommand) {
        User user = userService.create(userCreateCommand);
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
