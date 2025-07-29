package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.dto.command.UserCreateCommand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1ApiSpec;
import com.loopers.interfaces.api.user.UserV1Dto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {
    private final BrandFacade brandFacade;

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandV1Dto.InfoResponse> get(
        @PathVariable(value = "brandId") Long brandId
    ) {
        BrandInfo brandInfo = brandFacade.getBrand(brandId);
        if (brandInfo == null) {
            throw new EntityNotFoundException("해당 브랜드를 찾을 수 없습니다.");
        }

        BrandV1Dto.InfoResponse response = BrandV1Dto.InfoResponse.from(brandInfo);
        return ApiResponse.success(response);
    }
}
