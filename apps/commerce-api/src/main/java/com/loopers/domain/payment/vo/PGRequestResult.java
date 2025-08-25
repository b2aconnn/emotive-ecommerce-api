package com.loopers.domain.payment.vo;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorRequestResponse;

import static com.loopers.application.payment.dto.PaymentResultStatus.FAILED;

public record PGRequestResult(
        String transactionKey,
        PaymentResultStatus status,
        String reason
) {
    public static PGRequestResult from(PGSimulatorRequestResponse pgSimulatorRequestResponse) {
        return new PGRequestResult(
                pgSimulatorRequestResponse.data().transactionKey(),
                pgSimulatorRequestResponse.data().status(),
                pgSimulatorRequestResponse.data().reason()
        );
    }

    public void checkFailed() {
        if (this.status() == FAILED) {
            throw new IllegalStateException("결제 요청에 실패했습니다: " + this.reason());
        }
    }
}
