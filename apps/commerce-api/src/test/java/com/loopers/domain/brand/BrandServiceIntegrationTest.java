package com.loopers.domain.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BrandServiceIntegrationTest {

    @Autowired
    private BrandFacade brandFacade;
    @Autowired
    private BrandRepository brandRepository;

    @DisplayName("브랜드를 조회할 때, ")
    @Nested
    class GET {
        @DisplayName("브랜드가 존재하지 않은 경우, null이 반환된다.")
        @Test
        void getFail() {
            // arrange
            Long nonExistentBrandId = 999L;

            // act
            BrandInfo brandInfo = brandFacade.getBrand(nonExistentBrandId);

            // assert
            assertThat(brandInfo).isNull();
        }

        @DisplayName("해당 ID 의 브랜드가 존재할 경우, 브랜드 정보가 반환된다.")
        @Test
        void returnsBrandInfoWhenBrandExists() {
            // arrange
            String brandName = "Test Brand";
            String logoUrl = "http://example.com/logo.png";
            String description = "This is a test brand.";

            BrandCreateCommand brandCreateCommand = new BrandCreateCommand(brandName, logoUrl, description);
            Brand saveBrand = brandRepository.save(Brand.create(brandCreateCommand));

            // act
            BrandInfo brandInfo = brandFacade.getBrand(saveBrand.getId());

            // assert
            assertThat(brandInfo.id()).isNotNull();
            assertThat(brandInfo.brandName()).isEqualTo(saveBrand.getName());
            assertThat(brandInfo.logoUrl()).isEqualTo(saveBrand.getLogoUrl());
            assertThat(brandInfo.description()).isEqualTo(saveBrand.getDescription());
        }
    }
}
