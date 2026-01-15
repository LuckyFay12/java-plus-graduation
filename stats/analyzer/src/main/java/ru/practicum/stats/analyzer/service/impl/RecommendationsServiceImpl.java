package ru.practicum.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.model.EventSimilarity;
import ru.practicum.stats.analyzer.model.Interaction;
import ru.practicum.stats.analyzer.repository.InteractionRepository;
import ru.practicum.stats.analyzer.repository.SimilarityRepository;
import ru.practicum.stats.analyzer.service.RecommendationsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationsServiceImpl implements RecommendationsService {
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int maxResults = (int) request.getMaxResults();

        log.info("Рекомендации для пользователя {}", userId);

        // Получаем взаимодействия пользователя
        List<Interaction> userInteractions = interactionRepository
                .findByUserIdOrderByTimestampDesc(userId);

        if (userInteractions.isEmpty()) {
            return Stream.empty();
        }

        // События, с которыми уже взаимодействовал
        Set<Long> userEventIds = userInteractions.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        // Map рейтингов
        Map<Long, Double> eventRatings = userInteractions.stream()
                .collect(Collectors.toMap(
                        Interaction::getEventId,
                        Interaction::getRating,
                        Math::max
                ));

        // Ищем похожие события
        List<EventSimilarity> similarities = similarityRepository
                .findByEventIdIn(userEventIds);

        Map<Long, Double> candidateScores = new HashMap<>();

        for (EventSimilarity sim : similarities) {
            Long candidateId = extractCandidate(sim, userEventIds);
            if (candidateId != null) {
                Long userEventId = sim.getEvent1().equals(candidateId)
                        ? sim.getEvent2()
                        : sim.getEvent1();

                Double rating = eventRatings.get(userEventId);
                if (rating != null) {
                    double score = rating * sim.getSimilarity();
                    candidateScores.merge(candidateId, score, Math::max);
                }
            }
        }

        return candidateScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxResults)
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build());
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        int maxResults = (int) request.getMaxResults();

        log.info("Похожие события для {}", eventId);

        return similarityRepository
                .findByEventIdOrderBySimilarityDesc(eventId, maxResults)
                .stream()
                .map(sim -> {
                    Long otherId = sim.getEvent1().equals(eventId)
                            ? sim.getEvent2()
                            : sim.getEvent1();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(otherId)
                            .setScore(sim.getSimilarity())
                            .build();
                });
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventsIds = request.getEventIdList();

        return eventsIds.stream()
                .distinct()
                .map(eventId -> {
                    Double sum = interactionRepository.sumMaxRatingPerUserByEventId(eventId);
                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(sum != null ? sum : 0.0)
                            .build();
                })
                .sorted((a,b) -> Double.compare(b.getScore(), a.getScore()));
    }

    private Long extractCandidate(EventSimilarity sim, Set<Long> userEventIds) {
        if (userEventIds.contains(sim.getEvent1())) {
            return sim.getEvent2();
        } else if (userEventIds.contains(sim.getEvent2())) {
            return sim.getEvent1();
        }
        return null;
    }
}
