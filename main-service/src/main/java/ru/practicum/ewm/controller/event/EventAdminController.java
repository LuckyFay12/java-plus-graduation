package ru.practicum.ewm.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventAdminFilter;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.entity.event.EventState;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.service.event.EventAdminService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventAdminController {

    private final EventAdminService eventAdminService;

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> userIds,
                                        @RequestParam(required = false) List<EventState> states,
                                        @RequestParam(required = false) List<Long> categoryIds,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение событий (admin)");
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        EventAdminFilter filter = EventAdminFilter.builder()
                .userIds(userIds)
                .states(states)
                .categoryIds(categoryIds)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build();
        return eventAdminService.getAll(filter, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@RequestBody @Valid UpdateEventAdminRequest request,
                                    @PathVariable @Positive Long eventId) {
        log.info("Запрос на обновление события id = {} и его статуса (admin)", eventId);
        return eventAdminService.update(request, eventId);
    }
}
