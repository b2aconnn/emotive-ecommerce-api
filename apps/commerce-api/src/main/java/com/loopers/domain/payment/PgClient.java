package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.PGRequest;
import com.loopers.domain.payment.vo.PGRequestResult;
import com.loopers.domain.payment.vo.PGTransactionInfoResult;

public interface PgClient {
    PGRequestResult requestPayment(PGRequest request);
    PGTransactionInfoResult getTransaction(String transactionKey);
}
