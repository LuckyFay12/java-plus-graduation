package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class RequestEventController {

    private final RequestService requestService;

    @GetMapping()
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение заявок на участие в событии id = {} пользователя id = {} (private)", eventId, userId);
        return requestService.getRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateEventRequest(@RequestBody @Valid EventRequestStatusUpdateRequest request,
                                                             @PathVariable @Positive Long userId,
                                                             @PathVariable @Positive Long eventId) {
        log.info("Запрос на изменение статуса заявок на участие в событии id = {} пользователя id = {} (private)", eventId, userId);
        return requestService.updateRequest(request, userId, eventId);
    }
}
