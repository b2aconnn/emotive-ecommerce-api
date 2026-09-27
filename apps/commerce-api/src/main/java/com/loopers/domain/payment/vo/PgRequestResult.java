package com.loopers.domain.payment.vo;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorRequestResponse;

public record PgRequestResult(String transactionKey, PaymentResultStatus status, String reason) {
    public static PgRequestResult from(PgSimulatorRequestResponse pgSimulatorRequestResponse) {
        return new PgRequestResult(
                pgSimulatorRequestResponse.data().transactionKey(),
                pgSimulatorRequestResponse.data().status(),
                pgSimulatorRequestResponse.data().reason());
    }

    public void checkFailed() {
        if (this.status() == PaymentResultStatus.FAILED) {
            throw new IllegalStateException("결제 요청에 실패했습니다: " + this.reason());
        }
    }
}
