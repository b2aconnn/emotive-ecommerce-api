package com.loopers.domain.product.message.model;

import java.time.ZonedDateTime;

public record ProductMessageEnvelope<T>(
        ProductMessageType eventType,
        String eventId,
        ZonedDateTime occurredAt,
        T payload
) {}
