package com.loopers.domain.product.message.model;

import java.time.ZonedDateTime;

public record ProductMessageEnvelope<T>(
        ProductMessageType eventType,
        String eventId,
        String aggregateId,
        String aggregateType,
        ZonedDateTime occurredAt,
        T payload
) {}
