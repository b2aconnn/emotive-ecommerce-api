package com.loopers.domain.productlike;

import com.loopers.domain.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product_like_count")
@Entity
public class ProductLikeCount {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
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
