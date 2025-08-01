package com.loopers.domain.productlike;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.dto.command.ProductCreateCommand;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product_like")
@Entity
public class ProductLike extends BaseEntity {

    @OneToOne(fetch = LAZY)
    private User user;

    @OneToOne(fetch = LAZY)
    private Product product;

    private ProductLike(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    public static ProductLike create(User user, Product product) {
        return new ProductLike(user, product);
    }
}
