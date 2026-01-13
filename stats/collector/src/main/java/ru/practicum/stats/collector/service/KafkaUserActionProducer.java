package ru.practicum.stats.collector.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import ru.practicum.stats.collector.config.KafkaTopic;
import ru.practicum.stats.collector.config.KafkaTopicConfig;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserActionProducer implements AutoCloseable {

    private final KafkaTemplate<Long, SpecificRecordBase> producer;
    private final KafkaTopicConfig topicConfig;

    public void send(UserActionAvro userAction, KafkaTopic topic) {
        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(
                        topicConfig.getTopics().get(topic),
                        null,
                        userAction.getTimestamp().toEpochMilli(),
                        userAction.getEventId(),
                        userAction);

        log.trace("Сохранение действия пользователя {} в топик {}",
                userAction.getClass().getSimpleName(), topic);

        CompletableFuture<SendResult<Long, SpecificRecordBase>> future = producer.send(record);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Действие пользователя {} связанное с событием с ID {} отправлено в topic {}", userAction.getClass().getSimpleName(), userAction.getEventId(), topic);
            } else {
                log.error("Ошибка отправки user action {}, error: {}", userAction.getClass().getSimpleName(), ex.getMessage());
            }
        });
    }

    @PreDestroy
    public void close() {
        log.info("Shutting down producer");
        producer.flush();
        producer.destroy();

    }
}