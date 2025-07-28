package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.data.UserCreateCommand;
import com.loopers.domain.user.type.GenderType;
import com.loopers.interfaces.api.ApiResponse;
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
import java.util.function.Function;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {
    private static final String ENDPOINT_CREATE = "/api/v1/users";
    private static final Function<String, String> ENDPOINT_GET = id -> "/api/v1/users/" + id;

    private final TestRestTemplate testRestTemplate;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(
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

    @DisplayName("POST /api/v1/users")
    @Nested
    class POST {
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsCreatedUserInfoOnSuccessfulRegistration() {
            // arrange
            String userId = "user1234";
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateCommand userCreateCommand = new UserCreateCommand(userId, name, email, birthDateString, gender);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserCreateCommand> requestEntity = new HttpEntity<>(userCreateCommand, headers);

            ParameterizedTypeReference<ApiResponse<UserV1Dto.CreateResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.CreateResponse>> response =
                testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(userCreateCommand.userId()),
                () -> assertThat(response.getBody().data().name()).isEqualTo(userCreateCommand.name()),
                () -> assertThat(response.getBody().data().email()).isEqualTo(userCreateCommand.email()),
                () -> assertThat(response.getBody().data().birthDate()).isEqualTo(LocalDate.of(2000, 1, 1))
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequestWhenGenderIsMissing() {
            // arrange
            String userId = "user1234";
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = null;
            UserCreateCommand userCreateCommand = new UserCreateCommand(userId, name, email, birthDateString, gender);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserCreateCommand> requestEntity = new HttpEntity<>(userCreateCommand, headers);

            ParameterizedTypeReference<ApiResponse<UserV1Dto.CreateResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.CreateResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("GET /api/v1/users/{id}")
    @Nested
    class GET {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfoOnSuccessfulRetrievalOfMyInfo() {
            // arrange
            String userId = "user1234";
            UserCreateCommand userCreateCommand = new UserCreateCommand(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            userRepository.save(User.create(userCreateCommand));

            String requestUrl = ENDPOINT_GET.apply(userId);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.MyInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.MyInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(userCreateCommand.userId()),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(userCreateCommand.name()),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(userCreateCommand.email()),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo(LocalDate.of(2000, 1, 1))
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFoundResponseWhenIdDoesNotExist() {
            // arrange
            String userId = "invalidUserId";

            String requestUrl = ENDPOINT_GET.apply(userId);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.MyInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.MyInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
