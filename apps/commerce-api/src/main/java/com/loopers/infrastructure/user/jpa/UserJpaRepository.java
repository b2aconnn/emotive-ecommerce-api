package com.loopers.infrastructure.user.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.user.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    List<User> findAllByIdIn(List<Long> ids);
}
