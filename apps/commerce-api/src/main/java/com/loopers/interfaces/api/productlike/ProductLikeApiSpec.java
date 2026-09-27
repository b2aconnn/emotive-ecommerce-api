package com.loopers.interfaces.api.productlike;

import java.util.List;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.ProductsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Like API", description = "Product Like API 입니다.")
public interface ProductLikeApiSpec {
    @Operation(summary = "상품 좋아요 등록", description = "상품에 좋아요를 등록합니다. 이미 좋아요한 상품이면 아무 동작도 하지 않습니다(멱등).")
    ApiResponse<Object> like(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)") Long userId,
            @Schema(name = "상품 ID", description = "좋아요할 상품 ID") Long productId);

    @Operation(summary = "상품 좋아요 취소", description = "상품 좋아요를 취소합니다. 좋아요하지 않은 상품이면 아무 동작도 하지 않습니다(멱등).")
    ApiResponse<Object> unlike(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)") Long userId,
            @Schema(name = "상품 ID", description = "좋아요 취소할 상품 ID") Long productId);

    @Operation(summary = "내가 좋아요한 상품 목록 조회", description = "요청자가 좋아요한 상품 목록을 조회합니다.")
    ApiResponse<List<ProductsResponse>> getLikedProducts(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)") Long userId);
}
