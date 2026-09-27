package com.loopers.infrastructure.user.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.fixture.user.UserFixture;
import com.loopers.utils.DatabaseCleanUp;

@SpringBootTest
class UserRepositoryImplIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("여러 사용자를 내부 PK 로 한 번에 조회할 때, ")
    @Nested
    class FindAllByIdIn {
        @DisplayName("요청한 PK 에 해당하는 사용자를 모두 반환한다.")
        @Test
        void returnsAllRequestedUsers() {
            // given
            User user = userFixture.save();
            User anotherUser = userFixture.save();

            // when
            List<User> users = userRepository.findAllByIdIn(List.of(user.getId(), anotherUser.getId()));

            // then
            assertThat(users).extracting(User::getId).containsExactlyInAnyOrder(user.getId(), anotherUser.getId());
        }

        @DisplayName("존재하지 않는 PK 가 섞여 있으면, 존재하는 사용자만 반환한다.")
        @Test
        void returnsOnlyExistingUsers() {
            // given
            User user = userFixture.save();

            // when
            List<User> users = userRepository.findAllByIdIn(List.of(user.getId(), 999_999L));

            // then
            assertThat(users).extracting(User::getId).containsExactly(user.getId());
        }

        @DisplayName("PK 목록이 비어 있으면, 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyListWhenIdsIsEmpty() {
            // given
            userFixture.save();

            // when
            List<User> users = userRepository.findAllByIdIn(List.of());

            // then
            assertThat(users).isEmpty();
        }
    }
}
