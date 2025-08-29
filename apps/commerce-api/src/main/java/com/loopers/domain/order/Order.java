package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.dto.OrderCreateInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.loopers.domain.order.OrderStatus.*;
import static com.loopers.support.validation.TextValidator.requireText;
import static jakarta.persistence.EnumType.STRING;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

    private Long userId;

    private String orderer;

    private String deliveryAddress;

    private String contactNumber;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    private Long usedPoints;

    private Long couponId;

    private Long totalAmount;

    @Enumerated(STRING)
    private OrderStatus status;

    private Order(OrderCreateInfo createInfo) {
        this.userId = createInfo.userId();
        this.orderer = createInfo.orderer();
        this.deliveryAddress = createInfo.deliveryAddress();
        this.contactNumber = createInfo.contactNumber();
        this.couponId = createInfo.couponId();
        this.usedPoints = createInfo.usedPoints();
        this.totalAmount = 0L;
        this.status = CREATED;
    }

    public static Order create(OrderCreateInfo createInfo) {
        validateRequiredOrderInfo(createInfo);
        return new Order(createInfo);
    }

    private static void validateRequiredOrderInfo(OrderCreateInfo createInfo) {
        requireNonNull(createInfo.userId());
        requireText(createInfo.orderer(), "주문자명을 입력해주세요.");
        requireText(createInfo.deliveryAddress(), "배송지를 입력해주세요.");
        requireText(createInfo.contactNumber(), "주문자 연락처를 입력해주세요.");
    }

    public void calculateTotalAmount(Long usePoint) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalStateException("주문 상품이 없습니다.");
        }

        long oderItemsTotalPrice = orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();

        this.totalAmount = oderItemsTotalPrice - usePoint;
    }

    public void pending() {
        if (!checkCreateStatus()) {
            throw new IllegalStateException("주문이 생성되지 않았습니다. 먼저 주문을 생성해주세요.");
        }
        this.status = PENDING;
    }

    public void complete() {
        if (!checkCreateStatus() && !checkPendingStatus()) {
            return;
        }
        this.status = COMPLETED;
    }

    public void cancel() {
        if (!checkCreateStatus() && !checkPendingStatus()) {
            return;
        }
        this.status = CANCELED;
    }

    private boolean checkCreateStatus() {
        return this.status == CREATED;
    }

    private boolean checkPendingStatus() {
        return this.status == PENDING;
    }

    public Long getItemTotalAmount() {
        return orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();
    }

    public void applyDiscount(List<Discount> discounts) {
        Long discountAmount = discounts.stream()
                .mapToLong(Discount::amount)
                .sum();

        this.totalAmount = Math.max(0L, getItemTotalAmount() - discountAmount - usedPoints);
    }

    public void updateOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
