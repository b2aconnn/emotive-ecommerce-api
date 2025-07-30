package com.loopers.interfaces.api.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateCommand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;
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

import java.util.function.Function;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest {
    private static final String ENDPOINT_CREATE = "/api/v1/points/charge";
    private static final Function<String, String> ENDPOINT_GET = id -> "/api/v1/points";

    private final TestRestTemplate testRestTemplate;
    private final UserRepository brandRepository;
    private final PointRepository pointRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        UserRepository brandRepository,
        PointRepository pointRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.brandRepository = brandRepository;
        this.pointRepository = pointRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/points/chage")
    @Nested
    class POST {
        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsTotalPointsAfterSuccessfulCharge() {
            // arrange
            String userId = "user1234";
            UserCreateCommand userCreateCommand = new UserCreateCommand(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            brandRepository.save(User.create(userCreateCommand));

            Integer amount = 10_000;
            PointV1Dto.ChargeRequest chargeRequest = new PointV1Dto.ChargeRequest(amount);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", userId);
            HttpEntity<PointV1Dto.ChargeRequest> requestEntity = new HttpEntity<>(chargeRequest, headers);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.ChargeResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.ChargeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().amount()).isGreaterThanOrEqualTo(amount)
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFoundResponseWhenUserDoesNotExist() {
            // arrange
            String userId = "invalidUserId";

            Integer amount = 10_000;
            PointV1Dto.ChargeRequest chargeRequest = new PointV1Dto.ChargeRequest(amount);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", userId);
            HttpEntity<PointV1Dto.ChargeRequest> requestEntity = new HttpEntity<>(chargeRequest, headers);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.ChargeResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.ChargeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
    }

    @DisplayName("GET /api/v1/points")
    @Nested
    class GET {
        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsUserPointsOnSuccessfulRetrieval() {
            // arrange
            String userId = "user1234";
            UserCreateCommand userCreateCommand = new UserCreateCommand(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            brandRepository.save(User.create(userCreateCommand));

            Point point = Point.create(userId);
            point.charge(10_000);
            pointRepository.save(point);

            String requestUrl = ENDPOINT_GET.apply(userId);

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.InfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.InfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(userCreateCommand.userId()),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(point.getAmount())
            );
        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequestWhenUserIdHeaderIsMissing() {
            // arrange
            String userId = "user1234";
            String requestUrl = ENDPOINT_GET.apply(userId);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.MyInfoResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.MyInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
