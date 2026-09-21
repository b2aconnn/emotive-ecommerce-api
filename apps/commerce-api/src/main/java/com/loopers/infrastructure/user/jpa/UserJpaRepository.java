package com.loopers.infrastructure.user.jpa;

import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    List<User> findAllByIdIn(List<Long> ids);
}
