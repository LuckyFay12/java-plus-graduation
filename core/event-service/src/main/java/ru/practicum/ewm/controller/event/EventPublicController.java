package ru.practicum.ewm.controller.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.ewm.dto.event.EventPublicFilter;
import ru.practicum.dto.EventShortDto;
import ru.practicum.ewm.entity.event.EventSort;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                         @RequestParam(required = false) List<Long> categoryIds,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeStart,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10") @Positive Integer size,
                                         HttpServletRequest httpServletRequest) {
        log.info("Запрос на получение событий с фильтрацией");
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        EventPublicFilter filter = EventPublicFilter.builder()
                .text(text)
                .categoryIds(categoryIds)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort != null ? EventSort.valueOf(sort.toUpperCase()) : null)
                .build();
        return eventService.getAll(filter, from, size, httpServletRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto geEventById(@PathVariable @Positive Long id,
                                    HttpServletRequest httpServletRequest) {
        log.info("Запрос на получение события id = {}", id);
        return eventService.getById(id, httpServletRequest);
    }
}