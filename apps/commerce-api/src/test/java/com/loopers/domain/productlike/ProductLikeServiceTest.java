package com.loopers.domain.productlike;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.dto.command.UserCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductLikeServiceTest {
    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private ProductLikeCountRepository productLikeCountRepository;

    @InjectMocks
    private ProductLikeService productLikeService;

    @Test
    @DisplayName("특정 사용자는 좋아요를 하지 않은 하나의 상품에 대해 좋아요를 할 수 있다.")
    void userCanLikeProductOnce() {
        // arrange
        UserCreateCommand userCreateCommand = new UserCreateCommand(
                "user1234",
                "park",
                "user@domain.com",
                "2000-01-01",
                MALE);
        User user = User.create(userCreateCommand);

        Brand brand = Brand.create(new BrandCreateCommand(
                "Test Brand",
                "http://example.com/logo.png",
                "This is a test brand."));

        Product product = Product.create(new ProductCreateCommand(
                "Test Product 1",
                "http://example.com/product1.png",
                "This is a test product 1.",
                10_000,
                10,
                brand));

        when(productLikeRepository.hasUserLikedProduct(user.getId(), product.getId())).thenReturn(false);
        when(productLikeCountRepository.findByProductId(product.getId())).thenReturn(Optional.empty());

        ArgumentCaptor<ProductLikeCount> productLikeCountCaptor = ArgumentCaptor.forClass(ProductLikeCount.class);

        // act
        productLikeService.likeProduct(user, product);

        // assert
        verify(productLikeRepository, times(1)).hasUserLikedProduct(user.getId(), product.getId());
        verify(productLikeRepository, times(1)).save(any(ProductLike.class));

        verify(productLikeCountRepository).save(productLikeCountCaptor.capture());
        ProductLikeCount savedProductLikeCount = productLikeCountCaptor.getValue();
        assertThat(savedProductLikeCount).isNotNull();
        assertThat(savedProductLikeCount.getProduct()).isEqualTo(product);
        assertThat(savedProductLikeCount.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 사용자는 좋아요를 한 하나의 상품에 대해 다시 좋아요를 할 수 없다.")
    void userCannotLikeProductTwice() {
        // arrange
        UserCreateCommand userCreateCommand = new UserCreateCommand(
                "user1234",
                "park",
                "user@domain.com",
                "2000-01-01",
                MALE);
        User user = User.create(userCreateCommand);

        Brand brand = Brand.create(new BrandCreateCommand(
                "Test Brand",
                "http://example.com/logo.png",
                "This is a test brand."));

        Product product = Product.create(new ProductCreateCommand(
                "Test Product 1",
                "http://example.com/product1.png",
                "This is a test product 1.",
                10_000,
                10,
                brand));

        when(productLikeRepository.hasUserLikedProduct(user.getId(), product.getId())).thenReturn(true);

        // act
        productLikeService.likeProduct(user, product);

        // assert
        verify(productLikeRepository, times(1)).hasUserLikedProduct(user.getId(), product.getId());
        verify(productLikeRepository, never()).save(any(ProductLike.class));
        verify(productLikeCountRepository, never()).save(any(ProductLikeCount.class));
    }

    @Test
    @DisplayName("특정 사용자는 좋아요를 한 하나의 상품에 대해 좋아요를 취소할 수 있다.")
    void userCanUnlikeProduct() {
        // arrange
        UserCreateCommand userCreateCommand = new UserCreateCommand(
                "user1234",
                "park",
                "user@domain.com",
                "2000-01-01",
                MALE);
        User user = User.create(userCreateCommand);

        Brand brand = Brand.create(new BrandCreateCommand(
                "Test Brand",
                "http://example.com/logo.png",
                "This is a test brand."));

        Product product = Product.create(new ProductCreateCommand(
                "Test Product 1",
                "http://example.com/product1.png",
                "This is a test product 1.",
                10_000,
                10,
                brand));

        // arrange
        when(productLikeRepository.findByUserAndProduct(user.getId(), product.getId()))
                .thenReturn(Optional.of(ProductLike.create(user, product)));

        ProductLikeCount productLikeCount = spy(ProductLikeCount.create(product));
        productLikeCount.increase();
        when(productLikeCountRepository.findByProductId(product.getId()))
                .thenReturn(Optional.of(productLikeCount));

        // act
        productLikeService.unlikeProduct(user, product);

        // assert
        verify(productLikeRepository, times(1)).findByUserAndProduct(user.getId(), product.getId());
        verify(productLikeRepository, times(1)).delete(any(ProductLike.class));
        verify(productLikeCount, times(1)).decrease();
    }

    @Test
    @DisplayName("특정 사용자는 좋아요를 취소하지 않은 하나의 상품에 대해 좋아요를 취소할 수 없다.")
    void userCannotUnlikeProductIfNotLiked() {
        // arrange
        UserCreateCommand userCreateCommand = new UserCreateCommand(
                "user1234",
                "park",
                "user@domain.com",
                "2000-01-01",
                MALE);
        User user = User.create(userCreateCommand);

        Brand brand = Brand.create(new BrandCreateCommand(
                "Test Brand",
                "http://example.com/logo.png",
                "This is a test brand."));

        Product product = Product.create(new ProductCreateCommand(
                "Test Product 1",
                "http://example.com/product1.png",
                "This is a test product 1.",
                10_000,
                10,
                brand));

        ProductLikeCount spyProductLikeCount = spy(ProductLikeCount.class);
        when(productLikeRepository.findByUserAndProduct(user.getId(), product.getId()))
                .thenReturn(Optional.empty());

        // act
        productLikeService.unlikeProduct(user, product);

        // assert
        verify(productLikeRepository, times(1)).findByUserAndProduct(user.getId(), product.getId());
        verify(productLikeRepository, never()).delete(any(ProductLike.class));
        verify(spyProductLikeCount, never()).decrease();
    }
}
