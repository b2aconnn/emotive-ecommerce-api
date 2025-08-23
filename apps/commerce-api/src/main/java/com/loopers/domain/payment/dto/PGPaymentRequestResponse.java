package com.loopers.domain.payment.dto;

public record PGPaymentRequestResponse(
    Meta meta,
    Data data
) {
    public record Meta(String result) {}
    public record Data(String transactionKey, String status, String reason) {}
}
