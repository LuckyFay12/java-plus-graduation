package ru.practicum.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    @Value("${app.kafka.topics.user-actions}")
    private String userActionTopic;

    @Value("${app.kafka.topics.events-similarity}")
    private String eventSimilarityTopic;

    @Value("${spring.kafka.consumer.poll-timeout}")
    private Duration pollTimeout;

    private final EventSimilarityService similarityService;

    private final KafkaConsumer<Long, UserActionAvro> consumer;

    private final KafkaProducer<String, EventSimilarityAvro> producer;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения. Останавливаем агрегатор.");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(Collections.singletonList(userActionTopic));

            log.info("Подписались на топик: {}", userActionTopic);

            while (true) {

                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(pollTimeout);

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    Long eventId = record.key();

                    List<EventSimilarityAvro> similarities = similarityService.processUserAction(record.value());

                    for (EventSimilarityAvro similarity : similarities) {
                        String key = similarity.getEventA() + "-" + similarity.getEventB();
                        ProducerRecord<String, EventSimilarityAvro> message =
                                new ProducerRecord<>(eventSimilarityTopic, key, similarity);

                        producer.send(message);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("Получен WakeupException - завершаем работу");
        } catch (Exception e) {
            log.error("Ошибка во время обработки", e);
        } finally {

            try {
                log.info("Сбрасываем данные из буфера продюсера");
                producer.flush();
                log.info("Фиксируем оффсеты обработанных сообщений.");
                consumer.commitSync();
                log.info("Все данные сохранены, можно завершать работу");

            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}