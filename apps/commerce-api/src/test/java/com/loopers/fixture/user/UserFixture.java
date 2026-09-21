package com.loopers.fixture.user;

import com.loopers.domain.user.User;

import java.util.List;

public interface UserFixture {
    User create();
    List<User> create(int count);
    User save();
    List<User> save(int count);

    /**
     * 이름이 확정된 사용자를 저장한다.
     * 무작위 생성은 이름이 빈 문자열일 수도 있어, 이름을 검증/스냅샷하는 테스트에서는 이 메서드를 쓴다.
     */
    User save(String name);
}
