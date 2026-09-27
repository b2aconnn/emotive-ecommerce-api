package com.loopers.domain.productlike;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 상품에 누른 좋아요 한 건.
 *
 * <p>취소는 하드 삭제다(행이 존재 = 현재 좋아요 유지). 따라서 좋아요 이력은 남지 않으며,
 * 집계는 항상 조회 시점의 스냅샷을 본다.
 *
 * <p>{@code user}/{@code product}는 {@code @ManyToOne}이다 — 한 사용자는 여러 상품에,
 * 한 상품은 여러 사용자에게 좋아요될 수 있다. ({@code @OneToOne}이면 Hibernate가 {@code user_id}와
 * {@code product_id}에 각각 단일 컬럼 유니크 인덱스를 만들어 사용자당 좋아요 1건만 허용하게 된다.)
 * "사용자당 상품 1건"이라는 실제 불변식은 복합 유니크 제약으로 표현한다.
 */
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
        name = "product_like",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_product_like_user_product",
                        columnNames = {"user_id", "product_id"}))
@Entity
public class ProductLike extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private ProductLike(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    public static ProductLike create(User user, Product product) {
        return new ProductLike(user, product);
    }
}
