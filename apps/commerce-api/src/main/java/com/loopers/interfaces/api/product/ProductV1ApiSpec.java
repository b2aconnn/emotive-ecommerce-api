package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductsCondition;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Product V1 API", description = "Product API 입니다.")
public interface ProductV1ApiSpec {
    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 조회합니다."
    )
    ApiResponse<List<ProductV1Dto.AllResponse>> getAll(
            ProductsCondition condition
    );

    @Operation(
        summary = "상품 정보 조회",
        description = "ID로 상품 정보를 조회합니다."
    )
    ApiResponse<ProductV1Dto.InfoResponse> get(
        @Schema(name = "상품 ID", description = "조회할 상풍 ID")
        Long brandId
    );
}
