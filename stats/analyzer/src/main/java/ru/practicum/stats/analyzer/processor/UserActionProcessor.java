package ru.practicum.stats.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.config.KafkaConfig;
import ru.practicum.stats.analyzer.service.UserActionService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserActionProcessor implements Runnable {

    private final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final List<String> topics;
    private final Duration pollTimeout;
    private final UserActionService userActionService;

    public UserActionProcessor(KafkaConfig config, UserActionService userActionService) {
        final KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(this.getClass().getSimpleName());
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();
        this.userActionService = userActionService;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("ShutdownHook: Получен сигнал завершения работы консьюмера.");
            consumer.wakeup();
        }));
    }

    @Override
    public void run() {
        try {
            log.trace("Подписка на топики {}.", topics);
            consumer.subscribe(topics);
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(pollTimeout);
                int count = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    log.trace("Обработка сообщения: топик={}, партиция={}, offset={}, key={}",
                            record.topic(), record.partition(), record.offset(), record.key());
                    userActionService.handle(record.value());
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

    private void manageOffsets(ConsumerRecord<String, UserActionAvro> record, int count,
                               KafkaConsumer<String, UserActionAvro> consumer) {
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