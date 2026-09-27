package com.loopers.interfaces.api.brand;

import java.util.List;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.dto.BrandInfoResponse;
import com.loopers.interfaces.api.brand.dto.BrandsRequest;
import com.loopers.interfaces.api.brand.dto.BrandsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Brand API", description = "Brand API 입니다.")
public interface BrandApiSpec {

    @Operation(summary = "브랜드 목록 조회", description = "브랜드 목록을 offset 기반 페이징으로 조회합니다. 브랜드명 부분 검색을 지원합니다.")
    ApiResponse<List<BrandsResponse>> getAll(BrandsRequest brandsRequest);

    @Operation(summary = "브랜드 정보 조회", description = "ID로 브랜드 정보를 조회합니다.")
    ApiResponse<BrandInfoResponse> get(@Schema(name = "브랜드 ID", description = "조회할 브랜드 ID") Long brandId);
}
