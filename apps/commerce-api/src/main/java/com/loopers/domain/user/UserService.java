package com.loopers.domain.user;

import com.loopers.domain.user.dto.data.UserCreateCommand;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Component
public class UserService {
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public User create(UserCreateCommand userCreateCommand) {
        User user = User.create(userCreateCommand);
        return userRepository.save(user);
    }

    public User get(String userId) {
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));
    }
}
