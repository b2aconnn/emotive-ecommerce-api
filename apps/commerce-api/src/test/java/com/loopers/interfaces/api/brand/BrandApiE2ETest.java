package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.dto.BrandsResponse;
import com.loopers.interfaces.api.brand.dto.BrandInfoResponse;
import com.loopers.interfaces.api.point.dto.PointChargeRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrandApiE2ETest {
    private static final Function<Long, String> ENDPOINT_GET = id -> "/api/brands/" + id;
    private static final String ENDPOINT_GET_ALL = "/api/brands";

    private final TestRestTemplate testRestTemplate;
    private final BrandRepository brandRepository;
    private final BrandFixture brandFixture;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public BrandApiE2ETest(
        TestRestTemplate testRestTemplate,
        BrandRepository brandRepository,
        BrandFixture brandFixture,
        UserRepository userRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.brandRepository = brandRepository;
        this.brandFixture = brandFixture;
        this.userRepository = userRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/brands/{brandId}")
    @Nested
    class GET {
        @DisplayName("브랜드 조회에 성공할 경우, 해당하는 브랜드 정보를 응답으로 반환한다.")
        @Test
        void returnsBrandInfoOnSuccessfulRetrievalOfBrandInfo() {
            // given
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            String brandName = "Test Brand";
            String logoUrl = "http://example.com/logo.png";
            String description = "This is a test brand.";

            BrandCreateCommand brandCreateCommand = new BrandCreateCommand(brandName, logoUrl, description);
            Brand saveBrand = brandRepository.save(Brand.create(brandCreateCommand));

            String requestUrl = ENDPOINT_GET.apply(saveBrand.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", String.valueOf(saveUser.getId()));
            HttpEntity<PointChargeRequest> requestEntity = new HttpEntity<>(headers);

            // when
            ParameterizedTypeReference<ApiResponse<BrandInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);

            // then
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
            // given
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            Long brandId = 999L;

            String requestUrl = ENDPOINT_GET.apply(brandId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", String.valueOf(saveUser.getId()));
            HttpEntity<PointChargeRequest> requestEntity = new HttpEntity<>(headers);

            // when
            ParameterizedTypeReference<ApiResponse<BrandInfoResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandInfoResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("GET /api/brands")
    @Nested
    class GET_ALL {
        private HttpEntity<Void> authorizedRequest() {
            User saveUser = userRepository.save(User.create(
                    new UserCreateInfo("park", "user@domain.com", "2000-01-01", MALE)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-USER-ID", String.valueOf(saveUser.getId()));
            return new HttpEntity<>(headers);
        }

        private ResponseEntity<ApiResponse<List<BrandsResponse>>> requestBrands(HttpEntity<Void> requestEntity) {
            return requestBrands(requestEntity, "");
        }

        private ResponseEntity<ApiResponse<List<BrandsResponse>>> requestBrands(HttpEntity<Void> requestEntity, String queryString) {
            ParameterizedTypeReference<ApiResponse<List<BrandsResponse>>> responseType = new ParameterizedTypeReference<>() {};
            return testRestTemplate.exchange(ENDPOINT_GET_ALL + queryString, HttpMethod.GET, requestEntity, responseType);
        }

        private List<Brand> saveBrands(String... names) {
            List<Brand> saved = new ArrayList<>();
            for (String name : names) {
                saved.add(brandFixture.save(name));
            }
            return saved;
        }

        @DisplayName("브랜드가 존재할 경우, 200 응답과 함께 전체 브랜드 목록을 반환한다.")
        @Test
        void returnsAllBrandsOnSuccessfulRetrieval() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            Brand nike = brandFixture.save("Nike");
            Brand adidas = brandFixture.save("Adidas");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).hasSize(2),
                    () -> assertThat(response.getBody().data())
                            .extracting(BrandsResponse::id, BrandsResponse::brandName, BrandsResponse::logoUrl, BrandsResponse::description)
                            .containsExactlyInAnyOrder(
                                    tuple(nike.getId(), nike.getName(), nike.getLogoUrl(), nike.getDescription()),
                                    tuple(adidas.getId(), adidas.getName(), adidas.getLogoUrl(), adidas.getDescription())
                            )
            );
        }

        @DisplayName("브랜드가 하나도 존재하지 않을 경우, 200 응답과 함께 빈 배열을 반환한다.")
        @Test
        void returnsEmptyArrayWhenNoBrandExists() {
            // given
            databaseCleanUp.truncateAllTables();
            HttpEntity<Void> requestEntity = authorizedRequest();

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).isNotNull(),
                    () -> assertThat(response.getBody().data()).isEmpty()
            );
        }

        @DisplayName("offset 과 size 쿼리 파라미터를 넘길 경우, 해당 구간의 브랜드만 반환된다.")
        @Test
        void bindsOffsetAndSizeQueryParameters() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            List<Brand> saved = saveBrands("Brand1", "Brand2", "Brand3", "Brand4", "Brand5");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity, "?offset=2&size=2");

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).hasSize(2),
                    () -> assertThat(response.getBody().data()).extracting(BrandsResponse::id)
                            .containsExactly(saved.get(2).getId(), saved.get(3).getId())
            );
        }

        @DisplayName("searchKeyword 쿼리 파라미터를 넘길 경우, 브랜드명이 부분 일치하는 브랜드만 반환된다.")
        @Test
        void bindsSearchKeywordQueryParameter() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            saveBrands("Nike", "Adidas", "Puma");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity, "?searchKeyword=nike");

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).extracting(BrandsResponse::brandName)
                            .containsExactly("Nike")
            );
        }

        @DisplayName("searchKeyword 가 빈 문자열일 경우, 검색 조건 없이 전체 브랜드가 반환된다.")
        @Test
        void returnsAllBrandsWhenSearchKeywordQueryParameterIsBlank() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            saveBrands("Nike", "Adidas", "Puma");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity, "?searchKeyword=");

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).hasSize(3)
            );
        }

        @DisplayName("searchKeyword 에 일치하는 브랜드가 없을 경우, 200 응답과 함께 빈 배열을 반환한다.")
        @Test
        void returnsEmptyArrayWhenNoBrandMatchesSearchKeyword() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            saveBrands("Nike", "Adidas");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity, "?searchKeyword=존재하지않는브랜드");

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).isNotNull(),
                    () -> assertThat(response.getBody().data()).isEmpty()
            );
        }

        @DisplayName("쿼리 파라미터를 넘기지 않을 경우, 기본값(offset=0, size=20)으로 조회된다.")
        @Test
        void appliesDefaultPagingWhenNoQueryParameterGiven() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            saveBrands("Brand1", "Brand2", "Brand3");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> response = requestBrands(requestEntity);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data()).hasSize(3)
            );
        }

        @DisplayName("목록 조회 경로가 추가되어도, 단건 조회 경로는 그대로 동작한다. (라우팅 충돌 없음)")
        @Test
        void singleBrandLookupStillWorksAlongsideListLookup() {
            // given
            HttpEntity<Void> requestEntity = authorizedRequest();
            Brand nike = brandFixture.save("Nike");
            brandFixture.save("Adidas");

            // when
            ResponseEntity<ApiResponse<List<BrandsResponse>>> listResponse = requestBrands(requestEntity);

            ParameterizedTypeReference<ApiResponse<BrandInfoResponse>> singleResponseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandInfoResponse>> singleResponse =
                    testRestTemplate.exchange(ENDPOINT_GET.apply(nike.getId()), HttpMethod.GET, requestEntity, singleResponseType);

            // then
            assertAll(
                    () -> assertTrue(listResponse.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(listResponse.getBody().data()).hasSize(2),
                    () -> assertTrue(singleResponse.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(singleResponse.getBody().data().brandId()).isEqualTo(nike.getId()),
                    () -> assertThat(singleResponse.getBody().data().brandName()).isEqualTo(nike.getName())
            );
        }
    }
}
