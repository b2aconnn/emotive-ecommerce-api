package com.loopers.interfaces.api.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.dto.PointChargeRequest;
import com.loopers.interfaces.api.point.dto.PointChargeResponse;
import com.loopers.interfaces.api.point.dto.PointInfoResponse;
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

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointApiE2ETest {
    private static final String ENDPOINT_CREATE = "/api/points/charge";
    private static final String ENDPOINT_GET = "/api/points";

    private final TestRestTemplate testRestTemplate;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointApiE2ETest(
        TestRestTemplate testRestTemplate,
        UserRepository userRepository,
        PointRepository pointRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/points/chage")
    @Nested
    class POST {
        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsTotalPointsAfterSuccessfulCharge() {
            // arrange
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            Long amount = 10_000L;
            PointChargeRequest chargeRequest = new PointChargeRequest(amount);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", String.valueOf(saveUser.getId()));
            HttpEntity<PointChargeRequest> requestEntity = new HttpEntity<>(chargeRequest, headers);

            ParameterizedTypeReference<ApiResponse<PointChargeResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointChargeResponse>> response =
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
            Long amount = 10_000L;
            PointChargeRequest chargeRequest = new PointChargeRequest(amount);

            String requestUrl = ENDPOINT_CREATE;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", "999999");
            HttpEntity<PointChargeRequest> requestEntity = new HttpEntity<>(chargeRequest, headers);

            ParameterizedTypeReference<ApiResponse<PointChargeResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointChargeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
    }

    @DisplayName("GET /api/points")
    @Nested
    class GET {
        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsUserPointsOnSuccessfulRetrieval() {
            // arrange
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            Point point = Point.create(saveUser.getId());
            point.charge(10_000L);
            pointRepository.save(point);

            String requestUrl = ENDPOINT_GET;

            // act
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", String.valueOf(saveUser.getId()));
            HttpEntity requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<ApiResponse<PointInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(saveUser.getId()),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(point.getBalance())
            );
        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequestWhenUserIdHeaderIsMissing() {
            // arrange
            String requestUrl = ENDPOINT_GET;

            // act
            ParameterizedTypeReference<ApiResponse<UserMyInfoResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserMyInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
