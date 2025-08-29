package com.loopers.domain.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    List<User> saveAll(List<User> user);

    Optional<User> findById(Long userId);
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
