package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@RequiredArgsConstructor(access = PROTECTED)
@Table(name = "order_item")
@Entity
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Product product;

    private Integer quantity;
    private Integer price;

    private OrderItem(Order order, Product product, Integer quantity, Integer price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderItem create(Order order, Product product, Integer quantity, Integer price) {
        return new OrderItem(order, product, quantity, price);
    }
}
