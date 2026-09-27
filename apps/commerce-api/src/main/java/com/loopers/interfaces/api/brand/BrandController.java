package com.loopers.interfaces.api.brand;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.dto.BrandResult;
import com.loopers.application.brand.dto.BrandsResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.dto.BrandInfoResponse;
import com.loopers.interfaces.api.brand.dto.BrandsRequest;
import com.loopers.interfaces.api.brand.dto.BrandsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController implements BrandApiSpec {
    private final BrandService brandService;

    @Override
    @GetMapping("")
    public ApiResponse<List<BrandsResponse>> getAll(@ModelAttribute BrandsRequest brandsRequest) {
        List<BrandsResult> brands = brandService.getAll(brandsRequest.toCondition());

        List<BrandsResponse> response =
                brands.stream().map(BrandsResponse::from).toList();

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{brandId}")
    public ApiResponse<BrandInfoResponse> get(@PathVariable(value = "brandId") Long brandId) {
        BrandResult brandResult = brandService.getBrand(brandId);

        BrandInfoResponse response = BrandInfoResponse.from(brandResult);

        return ApiResponse.success(response);
    }
}
