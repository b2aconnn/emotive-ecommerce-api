package com.loopers.infrastructure.product.kafka;

import com.loopers.domain.product.message.ProductMessagePublisher;
import com.loopers.domain.product.message.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class KafkaProductMessagePublisher implements ProductMessagePublisher {

    private final KafkaTemplate kafkaTemplate;

    @Value( "${kafka.product.order}")
    private String productTopic;

    @Value( "${kafka.topics.audit}")
    private String auditTopic;

    @Override
    public void publishLikeAdded(ProductLikeAddedMessage message) {
        ProductMessageEnvelope<ProductLikeAddedMessage> messageEnvelope = new ProductMessageEnvelope<>(
                ProductMessageType.LIKE_ADDED,
                UUID.randomUUID().toString(),
                ZonedDateTime.now(),
                message
        );

        kafkaTemplate.send(productTopic, Long.toString(message.productId()), messageEnvelope);
        kafkaTemplate.send(auditTopic, messageEnvelope);
    }

    @Override
    public void publishLikeRemoved(ProductLikeRemovedMessage message) {
        ProductMessageEnvelope<ProductLikeRemovedMessage> messageEnvelope = new ProductMessageEnvelope<>(
                ProductMessageType.LIKE_ADDED,
                UUID.randomUUID().toString(),
                ZonedDateTime.now(),
                message
        );

        kafkaTemplate.send(productTopic, Long.toString(message.productId()), messageEnvelope);
        kafkaTemplate.send(auditTopic, messageEnvelope);
    }

    @Override
    public void publishViewed(ProductViewedMessage message) {
        ProductMessageEnvelope<ProductViewedMessage> messageEnvelope = new ProductMessageEnvelope<>(
                ProductMessageType.LIKE_ADDED,
                UUID.randomUUID().toString(),
                ZonedDateTime.now(),
                message
        );

        kafkaTemplate.send(productTopic, Long.toString(message.productId()), messageEnvelope);
        kafkaTemplate.send(auditTopic, messageEnvelope);
    }
}
