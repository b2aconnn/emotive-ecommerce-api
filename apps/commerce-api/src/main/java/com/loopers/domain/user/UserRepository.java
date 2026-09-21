package com.loopers.domain.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    List<User> saveAll(List<User> user);

    Optional<User> findById(Long userId);
    boolean existsById(Long userId);

    /**
     * 여러 사용자를 내부 PK로 한 번에 조회한다(사용자별 반복 조회 금지).
     * 집계 시 사용자 이름 스냅샷을 채우는 데 쓴다.
     *
     * @param ids 조회할 사용자 내부 PK 목록. 비어 있으면 빈 리스트를 반환한다.
     * @return 존재하는 사용자만 포함된 목록. 없으면 빈 리스트(null 아님).
     */
    List<User> findAllByIdIn(List<Long> ids);
}
