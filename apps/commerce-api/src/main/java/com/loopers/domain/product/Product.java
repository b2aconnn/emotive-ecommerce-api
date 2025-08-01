package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.productlike.ProductLikeCount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

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
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Setter
    @OneToOne(mappedBy = "product")
    private ProductLikeCount productLikeCount;

    public Product(ProductCreateCommand createCommand) {
        this.name = createCommand.name();
        this.mainImageUrl = createCommand.mainImageUrl();
        this.description = createCommand.description();
        this.price = createCommand.price();
        this.stockQuantity = createCommand.stockQuantity();
        this.brand = createCommand.brand();
    }

    public static Product create(ProductCreateCommand createCommand) {
        return new Product(createCommand);
    }
}
