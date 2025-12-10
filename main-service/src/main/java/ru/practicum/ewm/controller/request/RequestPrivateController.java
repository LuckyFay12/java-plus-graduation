package ru.practicum.ewm.controller.request;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/requests")
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {
        log.info("Получен HTTP-запрос на запрос пользователя: {} присоединиться к событию {}", userId, eventId);
        ParticipationRequestDto participationRequestDto = requestService.createRequest(userId, eventId);
        log.info("Успешно обработан HTTP-запрос на запрос пользователя: {} присоединиться к событию {}", userId, eventId);
        return participationRequestDto;
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(
            @PathVariable @Positive Long userId) {
        log.info("Получен HTTP-запрос на запрос списка запросов на события от пользователя: {} ", userId);
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("Получен HTTP-запрос на запрос пользователя: {} отменить запрос {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
