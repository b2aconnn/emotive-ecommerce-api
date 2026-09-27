package com.loopers.interfaces.api.productlike;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.product.dto.ProductsResult;
import com.loopers.application.productlike.ProductLikeService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.ProductsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like/products")
public class ProductLikeController implements ProductLikeApiSpec {
    private final ProductLikeService productLikeService;

    @Override
    @PostMapping("/{productId}")
    public ApiResponse<Object> like(
            @RequestHeader("X-USER-ID") Long userId, @PathVariable(value = "productId") Long productId) {
        productLikeService.likeProduct(userId, productId);
        return ApiResponse.success();
    }

    @Override
    @DeleteMapping("/{productId}")
    public ApiResponse<Object> unlike(
            @RequestHeader("X-USER-ID") Long userId, @PathVariable(value = "productId") Long productId) {
        productLikeService.unlikeProduct(userId, productId);
        return ApiResponse.success();
    }

    @Override
    @GetMapping("")
    public ApiResponse<List<ProductsResponse>> getLikedProducts(@RequestHeader("X-USER-ID") Long userId) {
        List<ProductsResult> likedProducts = productLikeService.getLikedProducts(userId);

        List<ProductsResponse> response =
                likedProducts.stream().map(ProductsResponse::from).toList();
        return ApiResponse.success(response);
    }
}
