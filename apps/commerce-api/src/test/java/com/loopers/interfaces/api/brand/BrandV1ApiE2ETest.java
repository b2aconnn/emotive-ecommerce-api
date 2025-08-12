package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.PointV1Dto;
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
class BrandV1ApiE2ETest {
    private static final Function<Long, String> ENDPOINT_GET = id -> "/api/v1/brands/" + id;

    private final TestRestTemplate testRestTemplate;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public BrandV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        BrandRepository brandRepository,
        UserRepository userRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.brandRepository = brandRepository;
        this.userRepository = userRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/brands/{brandId}")
    @Nested
    class GET {
        @DisplayName("브랜드 조회에 성공할 경우, 해당하는 브랜드 정보를 응답으로 반환한다.")
        @Test
        void returnsBrandInfoOnSuccessfulRetrievalOfBrandInfo() {
            // arrange
            String userId = "user1234";
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            userRepository.save(User.create(userCreateInfo));

            String brandName = "Test Brand";
            String logoUrl = "http://example.com/logo.png";
            String description = "This is a test brand.";

            BrandCreateCommand brandCreateCommand = new BrandCreateCommand(brandName, logoUrl, description);
            Brand saveBrand = brandRepository.save(Brand.create(brandCreateCommand));

            String requestUrl = ENDPOINT_GET.apply(saveBrand.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", userId);
            HttpEntity<PointV1Dto.ChargeRequest> requestEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.InfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandV1Dto.InfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().brandId()).isEqualTo(saveBrand.getId()),
                    () -> assertThat(response.getBody().data().brandName()).isEqualTo(saveBrand.getName()),
                    () -> assertThat(response.getBody().data().logoUrl()).isEqualTo(saveBrand.getLogoUrl()),
                    () -> assertThat(response.getBody().data().description()).isEqualTo(saveBrand.getDescription())
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFoundResponseWhenIdDoesNotExist() {
            // arrange
            String userId = "user1234";
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            userRepository.save(User.create(userCreateInfo));

            Long brandId = 999L;

            String requestUrl = ENDPOINT_GET.apply(brandId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", userId);
            HttpEntity<PointV1Dto.ChargeRequest> requestEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.InfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandV1Dto.InfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
