package com.loopers.interfaces.api.product;

import java.util.List;

import com.loopers.application.product.dto.ProductsCondition;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.ProductInfoResponse;
import com.loopers.interfaces.api.product.dto.ProductsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product API", description = "Product API 입니다.")
public interface ProductApiSpec {
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다.")
    ApiResponse<List<ProductsResponse>> getAll(ProductsCondition condition);

    @Operation(summary = "상품 정보 조회", description = "ID로 상품 정보를 조회합니다.")
    ApiResponse<ProductInfoResponse> get(@Schema(name = "상품 ID", description = "조회할 상풍 ID") Long brandId);
}
