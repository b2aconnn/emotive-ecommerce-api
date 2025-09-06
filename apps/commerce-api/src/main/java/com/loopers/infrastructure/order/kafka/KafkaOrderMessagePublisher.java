package com.loopers.infrastructure.order.kafka;

import com.loopers.domain.order.message.OrderMessagePublisher;
import com.loopers.domain.order.message.model.OrderCompletedMessage;
import com.loopers.domain.order.message.model.OrderMessageEnvelope;
import com.loopers.domain.order.message.model.OrderMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class KafkaOrderMessagePublisher implements OrderMessagePublisher {

    private final KafkaTemplate kafkaTemplate;

    @Value( "${kafka.topics.order}")
    private String orderTopic;

    @Value( "${kafka.topics.audit}")
    private String auditTopic;

    @Override
    public void publishOrderCompleted(OrderCompletedMessage message) {
        OrderMessageEnvelope<OrderCompletedMessage> messageEnvelope = new OrderMessageEnvelope<>(
                OrderMessageType.ORDER_COMPLETED,
                UUID.randomUUID().toString(),
                Long.toString(message.orderId()),
                "ORDER",
                ZonedDateTime.now(),
                message
        );

        kafkaTemplate.send(orderTopic, Long.toString(message.orderId()), messageEnvelope);
        kafkaTemplate.send(auditTopic, messageEnvelope);
    }
}
