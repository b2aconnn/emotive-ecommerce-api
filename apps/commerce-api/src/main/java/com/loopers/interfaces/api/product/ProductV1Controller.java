package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductAppService;
import com.loopers.application.product.ProductResult;
import com.loopers.application.product.ProductsCondition;
import com.loopers.application.product.ProductsResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductAppService productAppService;

    @GetMapping("")
    @Override
    public ApiResponse<List<ProductV1Dto.AllResponse>> getAll(
        @ModelAttribute ProductsCondition condition) {

        List<ProductsResult> products = productAppService.getAll(condition);

        List<ProductV1Dto.AllResponse> response = products.stream()
                .map(ProductV1Dto.AllResponse::from)
                .toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.InfoResponse> get(
        @PathVariable(value = "productId") Long id) {

        ProductResult product = productAppService.getProduct(id);

        ProductV1Dto.InfoResponse response = ProductV1Dto.InfoResponse.from(product);
        return ApiResponse.success(response);
    }
}
