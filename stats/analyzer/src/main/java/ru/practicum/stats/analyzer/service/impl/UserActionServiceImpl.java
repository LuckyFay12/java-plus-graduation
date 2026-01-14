package ru.practicum.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.model.Interaction;
import ru.practicum.stats.analyzer.repository.InteractionRepository;
import ru.practicum.stats.analyzer.service.UserActionService;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserActionServiceImpl implements UserActionService {

    private final InteractionRepository interactionRepository;

    @Override
    public void handle(UserActionAvro avro) {
        log.info("Сохранение действия: userId={}, eventId={}, action={}",
                avro.getUserId(), avro.getEventId(), avro.getActionType());

        Double rating = convertActionToRating(avro.getActionType());

        interactionRepository
                .findByUserIdAndEventId(avro.getUserId(), avro.getEventId())
                .ifPresentOrElse(
                        existing -> updateAction(existing, rating, avro),
                        () -> createNew(avro, rating)
                );
    }

    private void createNew(UserActionAvro avro, Double rating) {
        Interaction interaction = new Interaction();
        interaction.setUserId(avro.getUserId());
        interaction.setEventId(avro.getEventId());
        interaction.setRating(rating);
        interaction.setTimestamp(avro.getTimestamp());
        interactionRepository.save(interaction);
    }

    private void updateAction(Interaction existing, Double rating, UserActionAvro avro) {
        if(rating > existing.getRating()) {
            existing.setRating(rating);
            existing.setTimestamp(avro.getTimestamp());
            interactionRepository.save(existing);
            log.debug("Обновлен рейтинг у взаимодействия пользователя с событием с id {}", existing.getId());
        }
    }

    private Double convertActionToRating(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестный тип: " + actionType);
        };
    }
}
