package com.loopers.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.productlike.ProductLikeCount;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
@Entity
public class Product extends BaseEntity {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private String mainImageUrl;

    private String description;

    private Long price;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "product")
    private ProductStock productStock;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "product")
    private ProductLikeCount productLikeCount;

    public Product(ProductCreateCommand createCommand) {
        validatePrice(createCommand);

        this.name = createCommand.name();
        this.mainImageUrl = createCommand.mainImageUrl();
        this.description = createCommand.description();
        this.price = createCommand.price();
        this.brand = createCommand.brand();
    }

    private static void validatePrice(ProductCreateCommand createCommand) {
        if (createCommand.price() < 0) {
            throw new IllegalArgumentException("가격을 정확히 입력주세요.");
        }
    }

    public static Product create(ProductCreateCommand createCommand) {
        return new Product(createCommand);
    }

    public void assignStock(ProductStock productStock) {
        this.productStock = productStock;
    }

    public void assignLikeCount(ProductLikeCount productLikeCount) {
        this.productLikeCount = productLikeCount;
    }

    public boolean isStockEnough(Long quantity) {
        return productStock.isStockEnough(quantity);
    }

    public void likeCountUp() {
        this.productLikeCount.increase();
    }

    public void unlikeCountDown() {
        this.productLikeCount.decrease();
    }
}
