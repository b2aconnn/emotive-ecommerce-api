package com.loopers.application.user;

import com.loopers.application.user.dto.UserResult;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResult create(UserCreateInfo userCreateInfo) {
        User user = User.create(userCreateInfo);
        return UserResult.from(userRepository.save(user));
    }

    public UserResult get(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));
        return UserResult.from(user);
    }
}
