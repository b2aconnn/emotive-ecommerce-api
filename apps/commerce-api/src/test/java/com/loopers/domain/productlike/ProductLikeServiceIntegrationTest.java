package com.loopers.domain.productlike;

import com.loopers.application.productlike.ProductLikeAppService;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateCommand;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductLikeServiceIntegrationTest {
    @Autowired
    private ProductLikeAppService productLikeAppService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @MockitoSpyBean
    private ProductLikeRepository productLikeRepository;

    @MockitoSpyBean
    private ProductLikeCountRepository productLikeCountRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 좋아요을 할 때, ")
    @Nested
    class Create {
        @DisplayName("유저가 존재하지 않으면, 좋아요를 할 수 없다.")
        @Test
        void userNotFound() {
            // act
            // assert
            assertThatThrownBy(() -> productLikeAppService.likeProduct("nonexistent-user", 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유저가 존재하지 않습니다. userId: nonexistent-user");
        }

        @DisplayName("상품이 존재하지 않으면, 좋아요를 할 수 없다.")
        @Test
        void productNotFound() {
            // arrange
            UserCreateCommand userCreateCommand = new UserCreateCommand(
                    "user1234",
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            userRepository.save(User.create(userCreateCommand));

            // act
            assertThatThrownBy(() -> productLikeAppService.likeProduct("user1234", 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품이 존재하지 않습니다. productId: 1");
        }

        @DisplayName("유저가 상품을 좋아요하면 유저가 좋아요한 이력이 생성되고, 상품 좋아요 카운트가 1 증가한다.")
        @Test
        void createProductLike() {
            // arrange
            User saveUser = userRepository.save(User.create(new UserCreateCommand(
                    "user1234",
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE)));

            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "brand1234",
                    "logoUrl@abc.com",
                    "Brand Name")));

            Product saveProduct = productRepository.save(Product.create(new ProductCreateCommand(
                    "product1234",
                    "Product Name",
                    "Product Description",
                    10_000,
                    10,
                    saveBrand)));

            // act
            productLikeAppService.likeProduct("user1234", saveProduct.getId());

            // assert
            Optional<ProductLikeCount> productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());

            assertAll(
                () -> assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isTrue(),
                () -> assertThat(productLikeCountOptional.isPresent()).isTrue(),
                () -> assertThat(productLikeCountOptional.get().getLikeCount()).isEqualTo(1),
                () -> verify(productLikeRepository, times(1))
                        .save(any(ProductLike.class)),
                () -> verify(productLikeCountRepository, times(1))
                        .save(any(ProductLikeCount.class))
            );
        }

        @DisplayName("특정 사용자는 좋아요를 한 하나의 상품에 대해 다시 좋아요를 하더라도 멱등성이 보장된다.")
        @Test
        void userCannotLikeProductTwice() {
            // arrange
            User saveUser = userRepository.save(User.create(new UserCreateCommand(
                    "user1234",
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE)));

            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "brand1234",
                    "logoUrl@abc.com",
                    "Brand Name")));

            Product saveProduct = productRepository.save(Product.create(new ProductCreateCommand(
                    "product1234",
                    "Product Name",
                    "Product Description",
                    10_000,
                    10,
                    saveBrand)));

            productLikeRepository.save(ProductLike.create(saveUser, saveProduct));
            ProductLikeCount productLikeCount = ProductLikeCount.create(saveProduct);
            productLikeCount.increase();
            productLikeCountRepository.save(productLikeCount);

            assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isTrue();

            Optional<ProductLikeCount> productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());
            assertThat(productLikeCountOptional.isPresent()).isTrue();
            assertThat(productLikeCountOptional.get().getLikeCount()).isEqualTo(1);

            // act
            productLikeAppService.likeProduct("user1234", saveProduct.getId());

            // assert
            assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isTrue();

            productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());
            assertThat(productLikeCountOptional.isPresent()).isTrue();
            assertThat(productLikeCountOptional.get().getLikeCount()).isEqualTo(1);
        }

        @DisplayName("유저가 좋아요한 상품이 있을 떄, 좋아요 취소를 하면 좋아요 이력이 삭제되고, 상품 좋아요 카운트가 1 감소한다.")
        @Test
        @Transactional
        void deleteProductLike() {
            // arrange
            User saveUser = userRepository.save(User.create(new UserCreateCommand(
                    "user1234",
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE)));

            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "brand1234",
                    "logoUrl@abc.com",
                    "Brand Name")));

            Product saveProduct = productRepository.save(Product.create(new ProductCreateCommand(
                    "product1234",
                    "Product Name",
                    "Product Description",
                    10_000,
                    10,
                    saveBrand)));

            productLikeRepository.save(ProductLike.create(saveUser, saveProduct));
            ProductLikeCount productLikeCount = ProductLikeCount.create(saveProduct);
            productLikeCount.increase();
            productLikeCountRepository.save(productLikeCount);

            assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isTrue();

            Optional<ProductLikeCount> productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());
            assertThat(productLikeCountOptional.isPresent()).isTrue();
            assertThat(productLikeCountOptional.get().getLikeCount()).isEqualTo(1);

            // act
            productLikeAppService.unlikeProduct("user1234", saveProduct.getId());

            // assert
            assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isFalse();

            productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());
            assertThat(productLikeCountOptional.isPresent()).isTrue();
            assertThat(productLikeCountOptional.get().getLikeCount()).isEqualTo(0);
        }

        @DisplayName("유저가 좋아요한 상품이 없을 때, 좋아요 취소를 하면 멱등성이 보장된다.")
        @Test
        void deleteProductLikeWhenNotLiked() {
            // arrange
            User saveUser = userRepository.save(User.create(new UserCreateCommand(
                    "user1234",
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE)));

            Brand saveBrand = brandRepository.save(Brand.create(new BrandCreateCommand(
                    "brand1234",
                    "logoUrl@abc.com",
                    "Brand Name")));

            Product saveProduct = productRepository.save(Product.create(new ProductCreateCommand(
                    "product1234",
                    "Product Name",
                    "Product Description",
                    10_000,
                    10,
                    saveBrand)));

            assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isFalse();

            Optional<ProductLikeCount> productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());
            assertThat(productLikeCountOptional.isPresent()).isFalse();

            // act
            productLikeAppService.unlikeProduct("user1234", saveProduct.getId());

            // assert
            assertThat(productLikeRepository.hasUserLikedProduct(saveUser.getId(), saveProduct.getId())).isFalse();

            productLikeCountOptional = productLikeCountRepository.findByProductId(saveProduct.getId());
            assertThat(productLikeCountOptional.isPresent()).isFalse();
        }
    }
}
