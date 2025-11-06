package com.collega.paymentgatewaycip.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventProducer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEvent(String topic, Object event) {

        LOGGER.info("Publish event={} to topic={}", event.getClass().getSimpleName(), topic);

        Message<Object> message = MessageBuilder
            .withPayload(event)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .build();

        kafkaTemplate.send(message);
    }
}
