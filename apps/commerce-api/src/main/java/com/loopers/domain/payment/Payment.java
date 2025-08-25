package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.loopers.domain.order.PaymentStatus.PENDING;
import static com.loopers.support.validation.TextValidator.requireText;
import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "payment")
@Entity
public class Payment extends BaseEntity {

    private Long orderId;

    private String pgOrderId;

    private String transactionKey;

    @Enumerated(STRING)
    private PaymentStatus status;

    @Enumerated(STRING)
    private PaymentMethod method;

    private Long amount;

    private String reason;

    private Payment(Long orderId, String pgOrderId, PaymentMethod method, Long amount) {
        this.orderId = orderId;
        this.pgOrderId = pgOrderId;
        this.status = PENDING;
        this.method = method;
        this.amount = amount;
    }

    public static Payment create(Long orderId, String pgOrderId, PaymentMethod method, Long amount) {
        validateRequiredPaymentInfo(orderId, pgOrderId, method, amount);
        return new Payment(orderId, pgOrderId, method, amount);
    }

    private static void validateRequiredPaymentInfo(Long orderId, String pgOrderId, PaymentMethod method, Long amount) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }

        requireText(pgOrderId, "유효하지 않는 PG 주문 ID입니다.");

        if (method == null) {
            throw new IllegalArgumentException("결제 방법은 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 필수이며, 0보다 커야 합니다.");
        }
    }

    public void updateResult(
            String transactionKey,
            PaymentStatus status,
            String reason
    ) {
        updateTransactionKey(transactionKey);
        updateStatus(status);
        updateReason(reason);
    }

    public void updateTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public void updateStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("결제 상태는 필수입니다.");
        }
        this.status = status;
    }

    public void updateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("결제 사유는 필수입니다.");
        }
        this.reason = reason;
    }


}
