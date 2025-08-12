package com.loopers.fixture.user;

import com.loopers.domain.user.User;

import java.util.List;

public interface UserFixture {
    User create();
    List<User> create(int count);
    User save();
    List<User> save(int count);
}
