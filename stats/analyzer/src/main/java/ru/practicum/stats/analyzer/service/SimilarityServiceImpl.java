package ru.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.model.EventSimilarity;
import ru.practicum.stats.analyzer.repository.SimilarityRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SimilarityServiceImpl implements SimilarityService {
    private final SimilarityRepository similarityRepository;

    @Override
    public void handle(EventSimilarityAvro avro) {
        log.info("Сохранение сходства событий {} и {}: {}", avro.getEventA(), avro.getEventB(), avro.getScore());

        // event1 всегда меньше event2
        Long event1 = Math.min(avro.getEventA(), avro.getEventB());
        Long event2 = Math.max(avro.getEventA(), avro.getEventB());

        similarityRepository
                .findByEvent1AndEvent2(event1, event2)
                .ifPresentOrElse(
                        existing -> updateSimilarity(existing, avro),
                        () -> createNew(event1, event2, avro)
                        
                );
    }

    private void updateSimilarity(EventSimilarity existing, EventSimilarityAvro avro) {
        existing.setSimilarity(avro.getScore());
        existing.setTimestamp(avro.getTimestamp());
        similarityRepository.save(existing);
        log.debug("Сходство мероприятий обновлено");
    }

    private void createNew(Long event1, Long event2, EventSimilarityAvro avro) {
        EventSimilarity similarity = new EventSimilarity();
        similarity.setEvent1(event1);
        similarity.setEvent2(event2);
        similarity.setSimilarity(avro.getScore());
        similarity.setTimestamp(avro.getTimestamp());

        similarityRepository.save(similarity);
        log.debug("Создано новое сходство между событиями с id {} и {}", event1, event2);
    }

}
