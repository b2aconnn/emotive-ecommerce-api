package com.loopers.interfaces.api.product;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.product.ProductService;
import com.loopers.application.product.dto.ProductResult;
import com.loopers.application.product.dto.ProductsCondition;
import com.loopers.application.product.dto.ProductsResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.ProductInfoResponse;
import com.loopers.interfaces.api.product.dto.ProductsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController implements ProductApiSpec {

    private final ProductService productService;

    @Override
    @GetMapping("")
    public ApiResponse<List<ProductsResponse>> getAll(@ModelAttribute ProductsCondition condition) {

        List<ProductsResult> products = productService.getAll(condition);

        List<ProductsResponse> response =
                products.stream().map(ProductsResponse::from).toList();
        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductInfoResponse> get(@PathVariable(value = "productId") Long id) {

        ProductResult product = productService.getProduct(id);

        ProductInfoResponse response = ProductInfoResponse.from(product);
        return ApiResponse.success(response);
    }
}
