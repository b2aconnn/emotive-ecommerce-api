package com.loopers.domain.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product_stock")
@Entity
public class ProductStock {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Long quantity;

//    @Version
//    private Long version;

    private ProductStock(Product product, Long quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public static ProductStock create(Product product, Long quantity) {
        return new ProductStock(product, quantity);
    }

    public void deduct(Long quantity) {
        validateStockNotEmpty();
        validateStockEnough(quantity);

        this.quantity -= quantity;
    }

    public void validateStockNotEmpty() {
        if (this.quantity <= 0) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }
    }

    public void validateStockEnough(Long quantity) {
        if (this.quantity < quantity) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }
    }


}
