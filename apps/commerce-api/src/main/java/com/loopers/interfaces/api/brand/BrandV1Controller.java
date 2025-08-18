package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandAppService;
import com.loopers.application.brand.BrandResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {
    private final BrandAppService brandAppService;

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandV1Dto.InfoResponse> get(
        @PathVariable(value = "brandId") Long brandId
    ) {
        BrandResult brandResult = brandAppService.getBrand(brandId);

        BrandV1Dto.InfoResponse response = BrandV1Dto.InfoResponse.from(brandResult);
        return ApiResponse.success(response);
    }
}
