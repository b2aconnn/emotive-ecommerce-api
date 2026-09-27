package com.loopers.infrastructure.user.jpa;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public List<User> saveAll(List<User> users) {
        return userJpaRepository.saveAll(users);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public boolean existsById(Long userId) {
        return userJpaRepository.existsById(userId);
    }

    @Override
    public List<User> findAllByIdIn(List<Long> ids) {
        // 빈 목록으로 IN () 쿼리를 만들지 않는다.
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return userJpaRepository.findAllByIdIn(ids);
    }
}
