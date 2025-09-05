package com.loopers.domain.order.message.model;

import java.time.ZonedDateTime;

public record OrderMessageEnvelope<T>(
        OrderMessageType eventType,
        String eventId,
        ZonedDateTime occurredAt,
        T payload
) {}
