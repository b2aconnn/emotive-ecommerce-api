package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.domain.user.type.GenderType;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.dto.UserCreateResponse;
import com.loopers.interfaces.api.user.dto.UserMyInfoResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDate;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiE2ETest {
    private static final String ENDPOINT_CREATE = "/api/users";
    private static final String ENDPOINT_GET_ME = "/api/users/me";

    private final TestRestTemplate testRestTemplate;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserApiE2ETest(
        TestRestTemplate testRestTemplate,
        UserRepository userRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userRepository = userRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/users")
    @Nested
    class POST {
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsCreatedUserInfoOnSuccessfulRegistration() {
            // arrange
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(name, email, birthDateString, gender);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserCreateInfo> requestEntity = new HttpEntity<>(userCreateInfo, headers);

            ParameterizedTypeReference<ApiResponse<UserCreateResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserCreateResponse>> response =
                testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().id()).isNotNull(),
                () -> assertThat(response.getBody().data().name()).isEqualTo(userCreateInfo.name()),
                () -> assertThat(response.getBody().data().email()).isEqualTo(userCreateInfo.email()),
                () -> assertThat(response.getBody().data().birthDate()).isEqualTo(LocalDate.of(2000, 1, 1))
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequestWhenGenderIsMissing() {
            // arrange
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = null;
            UserCreateInfo userCreateInfo = new UserCreateInfo(name, email, birthDateString, gender);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserCreateInfo> requestEntity = new HttpEntity<>(userCreateInfo, headers);

            ParameterizedTypeReference<ApiResponse<UserCreateResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserCreateResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("GET /api/users/me")
    @Nested
    class GET {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfoOnSuccessfulRetrievalOfMyInfo() {
            // arrange
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", String.valueOf(saveUser.getId()));

            // act
            ParameterizedTypeReference<ApiResponse<UserMyInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserMyInfoResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().id()).isEqualTo(saveUser.getId()),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(userCreateInfo.name()),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(userCreateInfo.email()),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo(LocalDate.of(2000, 1, 1))
            );
        }

        @DisplayName("존재하지 않는 X-USER-ID 로 조회할 경우, 401 Unauthorized 응답을 반환한다.")
        @Test
        void returnsUnauthorizedResponseWhenUserIdDoesNotExist() {
            // arrange
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "999999");

            // act
            ParameterizedTypeReference<ApiResponse<UserMyInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserMyInfoResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
