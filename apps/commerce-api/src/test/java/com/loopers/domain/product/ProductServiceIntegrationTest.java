package com.loopers.domain.product;

import com.loopers.application.product.*;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.productlike.ProductLikeCount;
import com.loopers.domain.productlike.ProductLikeCountRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.loopers.application.product.ProductsSortType.LIKES_DESC;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductServiceIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeCountRepository productLikeCountRepository;

    @Autowired
    private BrandRepository brandRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품를 조회할 때, ")
    @Nested
    class GET {
        @DisplayName("상품 목록이 존재하지 않은 경우, 빈 값을 반환된다.")
        @Test
        void returnsEmptyListIfNoProductsFound() {
            // act
            List<ProductsInfo> productsInfo = productService.getAll(new ProductsCond());

            // assert
            assertThat(productsInfo).isEmpty();
        }

        @DisplayName("검색어 없이 모든 상품 목록이 최신순으로 조회된다.")
        @Test
        void returnsAllProductsSortedByNewestWithoutKeyword() {
            // arrange
            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "Test Brand",
                    "http://example.com/logo.png",
                    "This is a test brand.")));

            productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product 1",
                    "http://example.com/product1.png",
                    "This is a test product 1.",
                    10_000,
                    10,
                    saveBrand)));

            productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product 2",
                    "http://example.com/product2.png",
                    "This is a test product 2.",
                    20_000,
                    20,
                    saveBrand)));

            // act
            List<ProductsInfo> productsInfo = productService.getAll(new ProductsCond());

            // assert
            assertThat(productsInfo).hasSize(2)
                    .extracting("productName", "brandName", "price", "mainImageUrl")
                    .containsExactlyInAnyOrder(
                            tuple("Test Product 1", "Test Brand", 10_000, "http://example.com/product1.png"),
                            tuple("Test Product 2", "Test Brand", 20_000, "http://example.com/product2.png")
                    );
        }

        @DisplayName("모든 상품 목록이 높은 좋아요 수 순으로 조회된다.")
        @Test
        void returnsAllProductsSortedByLikeCount() {
            // arrange
            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "Test Brand",
                    "http://example.com/logo.png",
                    "This is a test brand.")));

            Product saveProduct1 = productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product 1",
                    "http://example.com/product1.png",
                    "This is a test product 1.",
                    10_000,
                    10,
                    saveBrand)));

            ProductLikeCount productLikeCount1 = ProductLikeCount.create(saveProduct1);
            productLikeCount1.increase();
            ProductLikeCount saveLikeCount1 = productLikeCountRepository.save(productLikeCount1);
            saveProduct1.setProductLikeCount(saveLikeCount1);

            Product saveProduct2 = productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product 2",
                    "http://example.com/product2.png",
                    "This is a test product 2.",
                    20_000,
                    20,
                    saveBrand)));

            ProductLikeCount productLikeCount2 = ProductLikeCount.create(saveProduct2);
            productLikeCount2.increase();
            productLikeCount2.increase();
            ProductLikeCount saveLikeCount2 = productLikeCountRepository.save(productLikeCount2);
            saveProduct2.setProductLikeCount(saveLikeCount2);

            // act
            List<ProductsInfo> productsInfo = productService.getAll(new ProductsCond(LIKES_DESC));

            // assert
            assertThat(productsInfo).hasSize(2)
                    .extracting("productName", "brandName", "price", "mainImageUrl")
                    .containsExactly(
                            tuple("Test Product 2", "Test Brand", 20_000, "http://example.com/product2.png"),
                            tuple("Test Product 1", "Test Brand", 10_000, "http://example.com/product1.png")
                    );
        }

        @DisplayName("존재하지 않는 브랜드명을 검색할 입력할 경우, 빈 값을 반환해야 한다.")
        @Test
        void returnsEmptyListWhenBrandNameNotFoundInSearch() {
            // arrange
            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "테스트 브랜드",
                    "http://example.com/logo.png",
                    "This is a test brand.")));

            productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product 1",
                    "http://example.com/product1.png",
                    "This is a test product 1.",
                    10_000,
                    10,
                    saveBrand)));

            productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product 2",
                    "http://example.com/product2.png",
                    "This is a test product 2.",
                    20_000,
                    20,
                    saveBrand)));

            String nonExistentBrandName = "notExistBrand";
            ProductsCond productsCond = new ProductsCond(nonExistentBrandName, null);

            // act
            List<ProductsInfo> productsInfo = productService.getAll(productsCond);

            // assert
            assertThat(productsInfo).isEmpty();
        }

        @DisplayName("상품이 존재하지 않은 경우, null이 반환된다.")
        @Test
        void returnsNullIfProductNotFound() {
            // arrange
            Long nonExistId = 999L;

            // act
            ProductInfo productInfo = productService.getProduct(nonExistId);

            // assert
            assertThat(productInfo).isNull();
        }

        @DisplayName("해당 ID 의 상품이 존재할 경우, 상품 정보가 반환된다.")
        @Test
        void returnsProductInfoWhenProductExists() {
            // arrange
            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "Test Brand",
                    "http://example.com/logo.png",
                    "This is a test brand.")));

            Product saveProduct = productRepository.save(Product.create(new ProductCreateCommand(
                    "Test Product",
                    "http://example.com/product.png",
                    "This is a test product.",
                    10_000,
                    50,
                    saveBrand)));

            ProductLikeCount saveLikeCount = productLikeCountRepository.save(ProductLikeCount.create(saveProduct));
            saveProduct.setProductLikeCount(saveLikeCount);

            // act
            ProductInfo productInfo = productService.getProduct(saveProduct.getId());

            // assert
            assertThat(productInfo.id()).isNotNull();
            assertThat(productInfo.brandName()).isEqualTo(saveProduct.getBrand().getName());
            assertThat(productInfo.productName()).isEqualTo(saveProduct.getName());
            assertThat(productInfo.mainImageUrl()).isEqualTo(saveProduct.getMainImageUrl());
            assertThat(productInfo.description()).isEqualTo(saveProduct.getDescription());
            assertThat(productInfo.price()).isEqualTo(saveProduct.getPrice());
            assertThat(productInfo.stockQuantity()).isEqualTo(saveProduct.getStockQuantity());
            assertThat(productInfo.likeCount()).isEqualTo(saveProduct.getProductLikeCount().getLikeCount());
        }
    }
}
