package ru.practicum.stats.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.config.KafkaConfig;
import ru.practicum.stats.analyzer.service.SimilarityService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EventSimilarityProcessor {
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    private final KafkaConsumer<String, EventSimilarityAvro> consumer;
    private final List<String> topics;
    private final Duration pollTimeout;
    private final SimilarityService similarityService;

    public EventSimilarityProcessor(KafkaConfig config, SimilarityService similarityService) {
        final KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(this.getClass().getSimpleName());
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();
        this.similarityService = similarityService;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("ShutdownHook: Получен сигнал завершения работы консьюмера.");
            consumer.wakeup();
        }));
    }

    public void start() {
        try {
            log.trace("Подписка на топики {}.", topics);
            consumer.subscribe(topics);
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(pollTimeout);
                int count = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    log.trace("Обработка сообщения: топик={}, партиция={}, offset={}, key={}",
                            record.topic(), record.partition(), record.offset(), record.key());
                    similarityService.handle(record.value());
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignores) {
            log.info("WakeupException: Завершение работы.");
        } catch (Exception e) {
            log.error("Критическая ошибка в работе консьюмера", e);
        } finally {
            try {
                consumer.commitSync(offsets);
            } finally {
                log.info("Закрытие консьюмера.");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, EventSimilarityAvro> record, int count,
                               KafkaConsumer<String, EventSimilarityAvro> consumer) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        if (count % 100 == 0) {
            consumer.commitAsync(offsets, (committedOffsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}.", committedOffsets, exception);
                }
            });
        }
    }
}

