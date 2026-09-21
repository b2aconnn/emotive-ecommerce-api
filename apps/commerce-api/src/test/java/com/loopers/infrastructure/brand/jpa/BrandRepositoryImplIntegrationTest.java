package com.loopers.infrastructure.brand.jpa;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BrandRepositoryImplIntegrationTest {

    private static final int NO_OFFSET = 0;
    private static final int LARGE_ENOUGH_SIZE = 20;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandFixture brandFixture;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /** 저장 순서대로 id 가 증가하므로, 반환된 리스트의 순서가 곧 기대하는 id 오름차순이다. */
    private List<Brand> saveBrands(String... names) {
        List<Brand> saved = new ArrayList<>();
        for (String name : names) {
            saved.add(brandFixture.save(name));
        }
        return saved;
    }

    @DisplayName("브랜드 목록을 조회할 때, ")
    @Nested
    class FindAll {
        @DisplayName("저장된 브랜드가 N건이면, N건이 모두 반환된다.")
        @Test
        void returnsAllSavedBrands() {
            // given
            List<Brand> saved = saveBrands("Nike", "Adidas", "Puma");

            // when
            List<Brand> brands = brandRepository.findAll(null, NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).hasSize(3);
            assertThat(brands).extracting(Brand::getId)
                    .containsExactlyInAnyOrderElementsOf(saved.stream().map(Brand::getId).toList());
            assertThat(brands).extracting(Brand::getName)
                    .containsExactlyInAnyOrder("Nike", "Adidas", "Puma");
        }

        @DisplayName("저장된 브랜드가 하나도 없으면, null 이 아닌 빈 리스트가 반환된다.")
        @Test
        void returnsEmptyListWhenNoBrandExists() {
            // given
            databaseCleanUp.truncateAllTables();

            // when
            List<Brand> brands = brandRepository.findAll(null, NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).isNotNull();
            assertThat(brands).isEmpty();
        }

        @DisplayName("조회 결과는 id 오름차순으로 정렬되어 반환된다.")
        @Test
        void returnsBrandsOrderedByIdAsc() {
            // given
            saveBrands("Puma", "Nike", "Adidas", "Reebok", "Asics");

            // when
            List<Brand> brands = brandRepository.findAll(null, NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).extracting(Brand::getId).isSorted();
        }
    }

    @DisplayName("브랜드 목록을 페이징 조회할 때, ")
    @Nested
    class Paging {
        @DisplayName("offset 과 size 가 실제로 적용되어, 지정한 구간의 건수만 반환된다.")
        @Test
        void appliesOffsetAndSize() {
            // given
            List<Brand> saved = saveBrands("Brand1", "Brand2", "Brand3", "Brand4", "Brand5");

            // when
            List<Brand> brands = brandRepository.findAll(null, 2, 2);

            // then
            assertThat(brands).hasSize(2);
            assertThat(brands).extracting(Brand::getId)
                    .containsExactly(saved.get(2).getId(), saved.get(3).getId());
        }

        @DisplayName("페이지를 이어붙이면, 중복이나 누락 없이 전체 브랜드가 조회된다.")
        @Test
        void pagesCoverAllBrandsWithoutDuplicationOrOmission() {
            // given
            List<Brand> saved = saveBrands("Brand1", "Brand2", "Brand3", "Brand4", "Brand5");

            // when
            List<Brand> page1 = brandRepository.findAll(null, 0, 2);
            List<Brand> page2 = brandRepository.findAll(null, 2, 2);
            List<Brand> page3 = brandRepository.findAll(null, 4, 2);

            List<Long> collectedIds = new ArrayList<>();
            page1.forEach(brand -> collectedIds.add(brand.getId()));
            page2.forEach(brand -> collectedIds.add(brand.getId()));
            page3.forEach(brand -> collectedIds.add(brand.getId()));

            // then
            assertThat(page1).hasSize(2);
            assertThat(page2).hasSize(2);
            assertThat(page3).hasSize(1);
            assertThat(collectedIds).doesNotHaveDuplicates();
            assertThat(collectedIds).containsExactlyElementsOf(saved.stream().map(Brand::getId).toList());
        }

        @DisplayName("offset 이 전체 건수를 넘어가면, 빈 리스트가 반환된다.")
        @Test
        void returnsEmptyListWhenOffsetExceedsTotalCount() {
            // given
            saveBrands("Nike", "Adidas");

            // when
            List<Brand> brands = brandRepository.findAll(null, 100, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).isNotNull();
            assertThat(brands).isEmpty();
        }
    }

    @DisplayName("브랜드명으로 검색할 때, ")
    @Nested
    class SearchKeyword {
        @DisplayName("검색어가 비어있으면(null/빈문자열/공백), 검색 조건 없이 전체가 조회된다.")
        @ParameterizedTest(name = "searchKeyword = \"{0}\"")
        @NullSource
        @ValueSource(strings = {"", "   "})
        void returnsAllBrandsWhenSearchKeywordIsBlank(String searchKeyword) {
            // given
            saveBrands("Nike", "Adidas", "Puma");

            // when
            List<Brand> brands = brandRepository.findAll(searchKeyword, NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).hasSize(3);
        }

        @DisplayName("검색어가 브랜드명에 부분 일치하면, 해당 브랜드만 반환된다.")
        @Test
        void returnsBrandsPartiallyMatchingSearchKeyword() {
            // given
            saveBrands("나이키", "아디다스", "푸마");

            // when
            List<Brand> brands = brandRepository.findAll("나이", NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).extracting(Brand::getName).containsExactly("나이키");
        }

        @DisplayName("검색어는 대소문자를 구분하지 않는다.")
        @ParameterizedTest(name = "searchKeyword = \"{0}\"")
        @ValueSource(strings = {"nike", "NIKE", "NiKe"})
        void ignoresCaseOfSearchKeyword(String searchKeyword) {
            // given
            saveBrands("Nike", "Adidas");

            // when
            List<Brand> brands = brandRepository.findAll(searchKeyword, NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).extracting(Brand::getName).containsExactly("Nike");
        }

        @DisplayName("검색어에 일치하는 브랜드가 없으면, null 이 아닌 빈 리스트가 반환된다.")
        @Test
        void returnsEmptyListWhenNoBrandMatchesSearchKeyword() {
            // given
            saveBrands("Nike", "Adidas");

            // when
            List<Brand> brands = brandRepository.findAll("존재하지않는브랜드", NO_OFFSET, LARGE_ENOUGH_SIZE);

            // then
            assertThat(brands).isNotNull();
            assertThat(brands).isEmpty();
        }

        @DisplayName("검색어와 페이징이 함께 적용된다.")
        @Test
        void appliesSearchKeywordAndPagingTogether() {
            // given
            saveBrands("Nike Air", "Nike Zoom", "Nike Free", "Adidas");

            // when
            List<Brand> brands = brandRepository.findAll("Nike", 1, 2);

            // then
            assertThat(brands).extracting(Brand::getName).containsExactly("Nike Zoom", "Nike Free");
        }
    }
}
