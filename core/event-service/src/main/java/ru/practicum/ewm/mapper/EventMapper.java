package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.entity.category.Category;
import ru.practicum.ewm.entity.event.Event;
import ru.practicum.enums.EventState;
import ru.practicum.ewm.entity.event.Location;


@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface EventMapper {

    @Mapping(source = "category", target = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(source = "location", target = "location")
    @Mapping(source = "state", target = "state", qualifiedByName = "mapEventStateToString")
    @Mapping(target = "commentsCount", ignore = true)
    @Mapping(source = "requestModeration", target = "requestModeration")
    EventFullDto toEventFullDto(Event event);

    @Mapping(source = "category", target = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "commentsCount", ignore = true)
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "compilations", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Named("mapEventStateToString")
    default String mapEventStateToString(EventState state) {
        return state != null ? state.name() : null;
    }

    @Named("mapStringToEventState")
    default EventState mapStringToEventState(String state) {
        return state != null ? EventState.valueOf(state) : null;
    }

    default Event toEvent(NewEventDto newEventDto, Category category, UserShortDto user, Location location) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiatorId(user.getId())
                .location(location)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }
}