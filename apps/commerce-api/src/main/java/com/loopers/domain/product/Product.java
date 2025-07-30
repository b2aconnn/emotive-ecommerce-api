package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product")
@Entity
public class Product extends BaseEntity {

    private String name;

    private String mainImageUrl;

    private String description;

    private Integer price;

    private Integer stockQuantity;

    @ManyToOne
    private Brand brand;

    private Integer likeCount;

    public Product(ProductCreateCommand createCommand) {
        this.name = createCommand.name();
        this.mainImageUrl = createCommand.mainImageUrl();
        this.description = createCommand.description();
        this.price = createCommand.price();
        this.stockQuantity = createCommand.stockQuantity();
        this.brand = createCommand.brand();
        this.likeCount = 0;
    }

    public static Product create(ProductCreateCommand createCommand) {
        return new Product(createCommand);
    }
}
