package ru.practicum.ewm.service.event;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.entity.category.Category;
import ru.practicum.ewm.entity.event.*;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.LocationMapper;
import ru.practicum.ewm.repository.category.CategoryRepository;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.event.LocationRepository;
import ru.practicum.ewm.service.category.CategoryService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final UserClient userClient;
    private final CollectorClient collectorClient;
    private final RecommendationsClient recommendationsClient;
    private final CommentClient commentClient;
    private final RequestClient requestClient;

    @Override
    public List<EventFullDto> getAll(EventAdminFilter adminFilter, Integer from, Integer size) {

        List<EventState> eventStates = null;
        if (adminFilter.getStates() != null && !adminFilter.getStates().isEmpty()) {
            eventStates = adminFilter.getStates().stream()
                    .map(String::toUpperCase)
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }
        Specification<Event> specification = DbSpecification.getAdminSpecification(
                adminFilter.getUserIds(),
                eventStates,
                adminFilter.getCategoryIds(),
                adminFilter.getRangeStart(),
                adminFilter.getRangeEnd());
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        List<EventFullDto> eventFullDtos = toEventFullTolist(events);

        return eventFullDtos.stream()
                .sorted(Comparator.comparingLong(EventFullDto::getId).reversed())
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventAdminRequest request, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory()).orElseThrow(() ->
                    new NotFoundException("Категория с id = %d не найдена".formatted(request.getCategory()))));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(locationMapper.toLocation(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            setEventDate(event, String.valueOf(request.getEventDate()));
        }
        setState(event, request);

        Event savedEvent = eventRepository.save(event);

        return toEventFullDtoWithInitiator(savedEvent);
    }

    private void setState(Event event, UpdateEventAdminRequest request) {
        if (request.getStateAction() != null) {
            if (request.getStateAction() == StateAction.PUBLISH_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Событие id = %d уже опубликовано".formatted(event.getId()));
                } else if (event.getState() == EventState.REJECT) {
                    throw new ConflictException("Событие id = %d отменено".formatted(event.getId()));
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (request.getStateAction() == StateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отменить опубликованное событие");
                }
                event.setState(EventState.REJECT);
            }
        }
    }

    @Override
    public List<EventShortDto> getAll(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();

        return toEventShortDtoList(events);
    }

    @Override
    public EventFullDto create(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Указана дата начала события в прошлом");
        }
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        UserShortDto user = userClient.getUserById(userId);
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));

        Event event = eventMapper.toEvent(newEventDto, category, user, location);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setConfirmedRequests(0);
        event.setState(EventState.PENDING);
        Event savedEvent = eventRepository.save(event);

        return toEventFullDtoWithInitiator(savedEvent);
    }

    @Override
    public EventFullDto getByInitiatorId(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        return toEventFullDtoWithInitiator(event);
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventUserRequest request, Long userId, Long eventId) {
        Event event = checkUpdateEvent(userId, eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Событие id = %d не отменено и не в состоянии ожидания.".formatted(eventId));
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Время события указано раньше, чем через два часа от текущего момента");
        }
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(request.getCategory().getId()));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(locationMapper.toLocation(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ValidationException("Нельзя установить отрицательное значение лимита");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
        if (request.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        if (request.getEventDate() != null) {
            setEventDate(event, String.valueOf(request.getEventDate()));
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case REJECT_EVENT -> event.setState(EventState.REJECT);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case PUBLISH_EVENT -> event.setState(EventState.PUBLISHED);
            }
        }
        Event savedEvent = eventRepository.save(event);

        return toEventFullDtoWithInitiator(savedEvent);
    }

    private void setEventDate(Event event, String date) {
        if (date != null) {
            LocalDateTime eventDateTime;

            try {
                eventDateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                try {
                    eventDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException ex) {
                    throw new ValidationException("Неверный формат даты. Используйте yyyy-MM-ddTHH:mm:ss или yyyy-MM-dd HH:mm:ss");
                }
            }

            if (eventDateTime.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Указанная дата уже наступила");
            }
            event.setEventDate(eventDateTime);
        }
    }

    private Event checkUpdateEvent(Long userId, Long eventId) {
        Event event = getById(eventId);
        checkInitiator(userId, event);
        checkExistsUser(userId);

        return event;
    }

    @Override
    public List<EventShortDto> getAll(EventPublicFilter publicFilter, Integer from, Integer size,
                                      HttpServletRequest httpServletRequest) {
        publicFilter.validateDates();
        Specification<Event> specification = DbSpecification.getPublicSpecification(
                publicFilter.getText(),
                publicFilter.getCategoryIds(),
                publicFilter.getPaid(),
                publicFilter.getRangeStart(),
                publicFilter.getRangeEnd(),
                publicFilter.getOnlyAvailable());

        Sort sort = Optional.ofNullable(publicFilter.getSort())
                .map(s -> Sort.by(Sort.Direction.DESC, s == EventSort.EVENT_DATE ? "eventDate" : "views"))
                .orElse(Sort.unsorted());

        List<Event> events = eventRepository.findAll(specification,
                        PageRequest.of(from / size, size).withSort(sort))
                .getContent();

        return toEventShortDtoList(events);
    }

    @Override
    public EventFullDto getById(Long eventId, Long userId, HttpServletRequest httpServletRequest) {
        Event event = getById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("getById: Событие id = %d не опубликовано".formatted(eventId));
        }
        sendViewAction(userId, eventId);

        double rating = getEventRating(eventId);
        event.setRating(rating);

        log.info("Метод getById, рейтинг события {}: {}", eventId, rating);

        Long commentsCount = commentClient.getCountPublishedCommentsByEventId(eventId);

        EventFullDto eventFullDto = toEventFullDtoWithInitiator(event);
        eventFullDto.setCommentsCount(commentsCount);
        eventFullDto.setRating(rating);

        return eventFullDto;
    }

    @Override
    public Event getById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
    }

    @Override
    public EventFullDto getByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие id = %d не найдено".formatted(eventId)));
        return toEventFullDtoWithInitiator(event);
    }

    @Override
    public void incrementConfirmedRequests(Long eventId, int count) {
        int updated = eventRepository.incrementConfirmedRequestsNative(eventId, count);
        if (updated == 0) {
            throw new NotFoundException("Событие не найдено: " + eventId);
        }
    }

    @Override
    public List<EventShortDto> getRecommendations(long userId, int maxResult) {
        return recommendationsClient.getRecommendationsForUser(userId, maxResult)
                .map(recommendedEventProto -> {
                    Event event = getById(recommendedEventProto.getEventId());
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setRating(recommendedEventProto.getScore());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(Long eventId, long userId) {
        checkExistsUser(userId);
        Event event = getById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя лайкнуть неопубликованное событие");
        }
        if (!requestClient.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ValidationException("Нельзя лайкнуть событие, которое не посещал");
        }
        sendLikeAction(userId, eventId);
    }

    private void sendLikeAction(long userId, Long eventId) {
        try {
            UserActionProto userAction = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(ActionTypeProto.ACTION_LIKE)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();

            // Отправляем через gRPC клиент
            collectorClient.sendUserAction(userAction);
            log.debug("Отправлено действие LIKE: userId={}, eventId={}", userId, eventId);

        } catch (Exception e) {
            log.error("Ошибка отправки действия LIKE через gRPC: userId={}, eventId={}",
                    userId, eventId, e);
        }
    }

    private void sendViewAction(Long userId, Long eventId) {
        try {
            UserActionProto userAction = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(ActionTypeProto.ACTION_VIEW)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();

            // Отправляем через gRPC клиент
            collectorClient.sendUserAction(userAction);
            log.debug("Отправлено действие VIEW: userId={}, eventId={}", userId, eventId);

        } catch (Exception e) {
            log.error("Ошибка отправки действия VIEW через gRPC: userId={}, eventId={}",
                    userId, eventId, e);
        }
    }

    private double getEventRating(Long eventId) {
        try {
            Stream<RecommendedEventProto> stream = recommendationsClient.getInteractionsCount(eventId);
            return stream
                    .findFirst()
                    .map(RecommendedEventProto::getScore)
                    .orElse(0.0);

        } catch (Exception e) {
            log.error("Ошибка получения рейтинга через gRPC: eventId={}", eventId, e);
            return 0.0;
        }
    }

    private List<EventFullDto> toEventFullTolist(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .collect(Collectors.toList());

        List<UserShortDto> users = userClient.getUsersByIds(userIds);
        Map<Long, UserShortDto> userMap = users.stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setInitiator(userMap.get(event.getInitiatorId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private EventFullDto toEventFullDtoWithInitiator(Event event) {
        EventFullDto dto = eventMapper.toEventFullDto(event);

        try {
            UserShortDto initiator = userClient.getUserById(event.getInitiatorId());
            dto.setInitiator(initiator);
        } catch (FeignException e) {
            log.error("Не удалось получить пользователя с id {} для события {}",
                    event.getInitiatorId(), event.getId(), e);
        }
        return dto;
    }

    private List<EventShortDto> toEventShortDtoList(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .collect(Collectors.toList());

        List<UserShortDto> users = userClient.getUsersByIds(userIds);
        Map<Long, UserShortDto> userMap = users.stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setInitiator(userMap.get(event.getInitiatorId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }


    private void checkInitiator(Long userId, Event event) {
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ValidationException("Пользователь id = %d не является создателем события id = %d"
                    .formatted(userId, event.getId()));
        }
    }

    private void checkExistsUser(Long userId) {
        UserShortDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = %d не найден".formatted(userId));
        }
    }
}



