package ru.practicum.ewm.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.service.event.EventPrivateService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @GetMapping
    public List<EventShortDto> getPrivateEvents(@PathVariable @Positive Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение событий, добавленных пользователем id = {} (private)", userId);
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        return eventPrivateService.getAll(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@RequestBody @Valid NewEventDto newEventDto,
                                    @PathVariable @Positive Long userId) {
        log.info("Запрос на создание события пользователем id = {} (private)", userId);
        return eventPrivateService.create(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByInitiatorId(@PathVariable @Positive Long userId,
                                              @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение события id = {}, добавленного пользователем id = {} (private)", eventId, userId);
        return eventPrivateService.getByInitiatorId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByInitiatorId(@RequestBody @Valid UpdateEventUserRequest request,
                                                 @PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long eventId) {
        log.info("Запрос на изменение события id = {}, добавленного пользователем id = {} (private)", eventId, userId);
        return eventPrivateService.update(request, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение заявок на участие в событии id = {} пользователя id = {} (private)", eventId, userId);
        return eventPrivateService.getRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequest(@RequestBody @Valid EventRequestStatusUpdateRequest request,
                                                             @PathVariable @Positive Long userId,
                                                             @PathVariable @Positive Long eventId) {
        log.info("Запрос на изменение статуса заявок на участие в событии id = {} пользователя id = {} (private)", eventId, userId);
        return eventPrivateService.updateRequest(request, userId, eventId);
    }
}
