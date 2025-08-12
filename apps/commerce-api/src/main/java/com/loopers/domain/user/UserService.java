package com.loopers.domain.user;

import com.loopers.domain.user.dto.command.UserCreateInfo;
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
    public User create(UserCreateInfo userCreateInfo) {
        User user = User.create(userCreateInfo);
        return userRepository.save(user);
    }

    public User get(String userId) {
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("[userId = " + userId + "] 를 찾을 수 없습니다."));
    }

    public void existsById(Long userId) {
        boolean isExistsUser = userRepository.existsById(userId);
        if (isExistsUser == false) {
            throw new IllegalStateException("사용자가 존재하지 않습니다.");
        }
    }
}
