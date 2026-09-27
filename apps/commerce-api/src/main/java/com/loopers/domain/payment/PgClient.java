package com.loopers.domain.payment;

import com.loopers.domain.payment.dto.PgRequest;
import com.loopers.domain.payment.vo.PgRequestResult;
import com.loopers.domain.payment.vo.PgTransactionInfoResult;

public interface PgClient {
    PgRequestResult requestPayment(PgRequest request);

    PgTransactionInfoResult getTransaction(String transactionKey);
}
