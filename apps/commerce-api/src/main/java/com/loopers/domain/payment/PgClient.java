package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.PGPaymentRequestResponse;
import com.loopers.domain.payment.dto.PGRequest;
import com.loopers.domain.payment.dto.PGTransactionInfoResponse;

public interface PgClient {
    PGPaymentRequestResponse requestPayment(PGRequest request);
    PGTransactionInfoResponse getTransaction(String transactionKey);
}
