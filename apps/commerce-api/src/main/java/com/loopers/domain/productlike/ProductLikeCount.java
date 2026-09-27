package com.loopers.domain.productlike;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.loopers.domain.product.Product;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_like_count")
@Entity
public class ProductLikeCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer likeCount;

    private ProductLikeCount(Product product) {
        this.product = product;
        this.likeCount = 0;
    }

    public static ProductLikeCount create(Product product) {
        return new ProductLikeCount(product);
    }

    public void increase() {
        this.likeCount++;
    }

    public void decrease() {
        validatePositive();
        this.likeCount--;
    }

    private void validatePositive() {
        if (this.likeCount <= 0) {
            throw new IllegalStateException("좋아요 수는 0보다 작을 수 없습니다.");
        }
    }
}
