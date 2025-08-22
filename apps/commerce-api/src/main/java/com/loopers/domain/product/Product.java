package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.productlike.ProductLikeCount;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product")
@Entity
public class Product extends BaseEntity {

    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private String mainImageUrl;

    private String description;

    private Long price;

    @Setter
    @OneToOne(fetch = LAZY, mappedBy = "product")
    private ProductStock productStock;

    @Setter
    @OneToOne(fetch = LAZY, mappedBy = "product")
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
}
