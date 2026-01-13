package ru.practicum.stats.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class EventSimilarityService {

    // событие -> (пользователь -> максимальный вес)
    private final Map<Long, Map<Long, Double>> eventUserMaxWeights = new ConcurrentHashMap<>();

    //суммы для каждого события
    private final Map<Long, Double> eventWeightSums = new ConcurrentHashMap<>();

    // Map<EventId1, Map<EventId2, MinWeightSum>> - сумма минимальных весов для пар мероприятий
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    public List<EventSimilarityAvro> processUserAction(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();
        double newWeight = getWeight(action.getActionType());

        double oldWeight = getCurrentWeight(eventId, userId);

        if (newWeight <= oldWeight) {
            log.debug("Вес не увеличился: событие={}, пользователь={}, старый={}, новый={}",
                    eventId, userId, oldWeight, newWeight);
            return Collections.emptyList();
        }

        // Обновляем данные
        updateData(eventId, userId, newWeight, oldWeight);

        return calculateSimilarities(eventId, userId, oldWeight, newWeight, action.getTimestamp());
    }

    private void updateData(long eventId, long userId, double newWeight, double oldWeight) {
        Map<Long, Double> userWeights = eventUserMaxWeights
                .computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        userWeights.put(userId, newWeight);

        double difference = newWeight - oldWeight;
        double newSum = eventWeightSums.getOrDefault(eventId, 0.0) + difference;
        eventWeightSums.put(eventId, newSum);

        log.debug("Обновлено: событие={}, пользователь={}, вес {}→{}, сумма={}",
                eventId, userId, oldWeight, newWeight, newSum);
    }

    private List<EventSimilarityAvro> calculateSimilarities(
            long eventId, long userId, double oldWeight, double newWeight, Instant timestamp) {

        List<EventSimilarityAvro> results = new ArrayList<>();

        for (long otherEventId : eventUserMaxWeights.keySet()) {
            if (otherEventId == eventId) continue;

            Map<Long, Double> otherWeights = eventUserMaxWeights.get(otherEventId);
            Double otherWeight = (otherWeights != null) ? otherWeights.get(userId) : null;

            if (otherWeight == null) continue;

            // Обновляем S_min и считаем сходство
            double similarity = updateAndCalculate(
                    eventId, otherEventId, oldWeight, newWeight, otherWeight);

            if (similarity > 0) {
                results.add(createMessage(eventId, otherEventId, similarity, timestamp));
            }
        }

        return results;
    }

    private double updateAndCalculate(
            long eventId1, long eventId2,
            double oldWeight1, double newWeight1, double weight2) {

        long first = Math.min(eventId1, eventId2);
        long second = Math.max(eventId1, eventId2);

        // Обновляем S_min
        Map<Long, Double> pairSums = minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>());
        double currentMin = pairSums.getOrDefault(second, 0.0);

        double oldMin = Math.min(oldWeight1, weight2);
        double newMin = Math.min(newWeight1, weight2);
        double diff = newMin - oldMin;

        pairSums.put(second, currentMin + diff);

        // Считаем похожесть
        Double sum1 = eventWeightSums.get(first);
        Double sum2 = eventWeightSums.get(second);

        if (sum1 == null || sum2 == null || sum1 <= 0 || sum2 <= 0) {
            return 0.0;
        }

        double sMin = pairSums.get(second);
        return sMin / Math.sqrt(sum1 * sum2);
    }

    private double getCurrentWeight(long eventId, long userId) {
        Map<Long, Double> weights = eventUserMaxWeights.get(eventId);
        return (weights != null) ? weights.getOrDefault(userId, 0.0) : 0.0;
    }

    private EventSimilarityAvro createMessage(long eventId1, long eventId2,
                                              double similarity, Instant timestamp) {
        long first = Math.min(eventId1, eventId2);
        long second = Math.max(eventId1, eventId2);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();
    }

    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестный тип: " + actionType);
        };
    }
}