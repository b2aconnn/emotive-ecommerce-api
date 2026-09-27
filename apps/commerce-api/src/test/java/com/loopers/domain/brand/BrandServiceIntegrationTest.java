package com.loopers.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.dto.BrandResult;
import com.loopers.application.brand.dto.BrandsCondition;
import com.loopers.application.brand.dto.BrandsResult;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.utils.DatabaseCleanUp;

@SpringBootTest
public class BrandServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandFixture brandFixture;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /** 저장 순서대로 id 가 증가한다. */
    private List<Brand> saveBrands(String... names) {
        List<Brand> saved = new ArrayList<>();
        for (String name : names) {
            saved.add(brandFixture.save(name));
        }
        return saved;
    }

    @DisplayName("브랜드를 조회할 때, ")
    @Nested
    class GET {
        @DisplayName("브랜드가 존재하지 않은 경우, null이 반환된다.")
        @Test
        void getFail() {
            // given
            Long nonExistentBrandId = 999L;

            // when
            BrandResult brandResult = brandService.getBrand(nonExistentBrandId);

            // then
            assertThat(brandResult).isNull();
        }

        @DisplayName("해당 ID 의 브랜드가 존재할 경우, 브랜드 정보가 반환된다.")
        @Test
        void returnsBrandInfoWhenBrandExists() {
            // given
            String brandName = "Test Brand";
            String logoUrl = "http://example.com/logo.png";
            String description = "This is a test brand.";

            BrandCreateCommand brandCreateCommand = new BrandCreateCommand(brandName, logoUrl, description);
            Brand saveBrand = brandRepository.save(Brand.create(brandCreateCommand));

            // when
            BrandResult brandResult = brandService.getBrand(saveBrand.getId());

            // then
            assertThat(brandResult.id()).isNotNull();
            assertThat(brandResult.brandName()).isEqualTo(saveBrand.getName());
            assertThat(brandResult.logoUrl()).isEqualTo(saveBrand.getLogoUrl());
            assertThat(brandResult.description()).isEqualTo(saveBrand.getDescription());
        }
    }

    @DisplayName("브랜드 전체 목록을 조회할 때, ")
    @Nested
    class GET_ALL {
        @DisplayName("브랜드가 여러 건 존재할 경우, 모든 브랜드 정보가 반환된다.")
        @Test
        void returnsAllBrandsWhenBrandsExist() {
            // given
            Brand nike = brandRepository.save(
                    Brand.create(new BrandCreateCommand("Nike", "http://example.com/nike.png", "Just do it.")));
            Brand adidas = brandRepository.save(Brand.create(
                    new BrandCreateCommand("Adidas", "http://example.com/adidas.png", "Impossible is nothing.")));

            // when
            List<BrandsResult> brandsResults = brandService.getAll(new BrandsCondition());

            // then
            assertThat(brandsResults).hasSize(2);
            assertThat(brandsResults)
                    .extracting(
                            BrandsResult::id, BrandsResult::brandName, BrandsResult::logoUrl, BrandsResult::description)
                    .containsExactlyInAnyOrder(
                            tuple(nike.getId(), "Nike", "http://example.com/nike.png", "Just do it."),
                            tuple(adidas.getId(), "Adidas", "http://example.com/adidas.png", "Impossible is nothing."));
        }

        @DisplayName("브랜드가 하나도 존재하지 않을 경우, null 이 아닌 빈 리스트가 반환된다.")
        @Test
        void returnsEmptyListWhenNoBrandExists() {
            // given
            databaseCleanUp.truncateAllTables();

            // when
            List<BrandsResult> brandsResults = brandService.getAll(new BrandsCondition());

            // then
            assertThat(brandsResults).isNotNull();
            assertThat(brandsResults).isEmpty();
        }

        @DisplayName("offset 과 size 를 지정할 경우, 해당 구간의 브랜드만 반환된다.")
        @Test
        void appliesOffsetAndSizeFromCondition() {
            // given
            List<Brand> saved = saveBrands("Brand1", "Brand2", "Brand3", "Brand4", "Brand5");

            // when
            List<BrandsResult> brandsResults = brandService.getAll(new BrandsCondition(null, 2, 2));

            // then
            assertThat(brandsResults).hasSize(2);
            assertThat(brandsResults)
                    .extracting(BrandsResult::id)
                    .containsExactly(saved.get(2).getId(), saved.get(3).getId());
        }

        @DisplayName("offset 과 size 가 null 인 경우, 기본값(0, 20)이 채워져 조회된다.")
        @Test
        void fillsDefaultOffsetAndSizeWhenNull() {
            // given
            saveBrands("Brand1", "Brand2", "Brand3");

            // when
            List<BrandsResult> brandsResults = brandService.getAll(new BrandsCondition(null, null, null));

            // then
            assertThat(brandsResults).hasSize(3);
        }

        @DisplayName("검색어를 지정할 경우, 브랜드명이 부분 일치하는 브랜드만 반환된다.")
        @Test
        void returnsBrandsMatchingSearchKeyword() {
            // given
            saveBrands("Nike", "Adidas", "Puma");

            // when
            List<BrandsResult> brandsResults = brandService.getAll(new BrandsCondition("nike", null, null));

            // then
            assertThat(brandsResults).extracting(BrandsResult::brandName).containsExactly("Nike");
        }

        @DisplayName("검색어에 일치하는 브랜드가 없을 경우, null 이 아닌 빈 리스트가 반환된다.")
        @Test
        void returnsEmptyListWhenNoBrandMatchesSearchKeyword() {
            // given
            saveBrands("Nike", "Adidas");

            // when
            List<BrandsResult> brandsResults = brandService.getAll(new BrandsCondition("존재하지않는브랜드", null, null));

            // then
            assertThat(brandsResults).isNotNull();
            assertThat(brandsResults).isEmpty();
        }
    }
}
