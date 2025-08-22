package com.loopers.domain.payment.generator;

public class PgIdGenerator {
    public static String generatePgOrderId() {
        return "ORD_" + System.currentTimeMillis();
    }
}
