package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.support.validation.TextValidator;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.loopers.domain.order.OrderStatus.CREATED;
import static com.loopers.domain.order.PaymentStatus.COMPLETE;
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

    private String userName;

    private String deliveryAddress;

    private String contactNumber;

    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(STRING)
    private OrderStatus status;

    @Enumerated(STRING)
    private PaymentStatus paymentStatus;

    private Order(OrderCreateInfo createInfo) {
        this.userId = createInfo.userId();
        this.userName = createInfo.userName();
        this.deliveryAddress = createInfo.deliveryAddress();
        this.contactNumber = createInfo.contactNumber();
        this.orderItems = createInfo.orderItems();

        this.status = CREATED;
        this.paymentStatus = COMPLETE;
    }

    public static Order create(OrderCreateInfo createInfo) {
        validateRequiredOrderInfo(createInfo);
        validateProductExistence(createInfo.orderItems());

        return new Order(createInfo);
    }

    private static void validateRequiredOrderInfo(OrderCreateInfo createInfo) {
        requireNonNull(createInfo.userId());
        requireText(createInfo.userName(), "주문자명을 입력해주세요.");
        requireText(createInfo.deliveryAddress(), "배송지를 입력해주세요.");
        requireText(createInfo.contactNumber(), "주문자 연락처를 입력해주세요.");
    }

    private static void validateProductExistence(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }
    }
}
