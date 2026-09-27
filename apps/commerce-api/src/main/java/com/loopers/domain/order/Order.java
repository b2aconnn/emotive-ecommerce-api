package com.loopers.domain.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.support.validation.TextValidator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Order(OrderCreateInfo createInfo) {
        this.userId = createInfo.userId();
        this.orderer = createInfo.orderer();
        this.deliveryAddress = createInfo.deliveryAddress();
        this.contactNumber = createInfo.contactNumber();
        this.couponId = createInfo.couponId();
        this.usedPoints = createInfo.usedPoints();
        this.totalAmount = 0L;
        this.status = OrderStatus.CREATED;
    }

    public static Order create(OrderCreateInfo createInfo) {
        validateRequiredOrderInfo(createInfo);
        return new Order(createInfo);
    }

    private static void validateRequiredOrderInfo(OrderCreateInfo createInfo) {
        Objects.requireNonNull(createInfo.userId());
        TextValidator.requireText(createInfo.orderer(), "주문자명을 입력해주세요.");
        TextValidator.requireText(createInfo.deliveryAddress(), "배송지를 입력해주세요.");
        TextValidator.requireText(createInfo.contactNumber(), "주문자 연락처를 입력해주세요.");
    }

    public void calculateTotalAmount(Long usePoint) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalStateException("주문 상품이 없습니다.");
        }

        long oderItemsTotalPrice =
                orderItems.stream().mapToLong(OrderItem::getTotalPrice).sum();

        this.totalAmount = oderItemsTotalPrice - usePoint;
    }

    public void pending() {
        if (!checkCreateStatus()) {
            throw new IllegalStateException("주문이 생성되지 않았습니다. 먼저 주문을 생성해주세요.");
        }
        this.status = OrderStatus.PENDING;
    }

    public void complete() {
        if (!checkCreateStatus() && !checkPendingStatus()) {
            return;
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (!checkCreateStatus() && !checkPendingStatus()) {
            return;
        }
        this.status = OrderStatus.CANCELED;
    }

    private boolean checkCreateStatus() {
        return this.status == OrderStatus.CREATED;
    }

    private boolean checkPendingStatus() {
        return this.status == OrderStatus.PENDING;
    }

    public Long getItemTotalAmount() {
        return orderItems.stream().mapToLong(OrderItem::getTotalPrice).sum();
    }

    public void applyDiscount(List<Discount> discounts) {
        Long discountAmount = discounts.stream().mapToLong(Discount::amount).sum();

        this.totalAmount = Math.max(0L, getItemTotalAmount() - discountAmount - usedPoints);
    }

    public void updateOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
