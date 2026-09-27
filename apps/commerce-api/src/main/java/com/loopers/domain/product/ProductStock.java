package com.loopers.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_stock")
@Entity
public class ProductStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
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
        if (!isStockEnough(quantity)) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }
    }

    public void restoreStock(Long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("복원할 재고 수량은 0 이상이어야 합니다.");
        }
        this.quantity += quantity;
    }

    public boolean isStockEnough(Long quantity) {
        return this.quantity >= quantity;
    }
}
