package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.enums.RequestStatus;


@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(source = "event", target = "eventId")
    @Mapping(source = "requester", target = "requesterId")
    ParticipationRequest toEntity(ParticipationRequestDto dto);

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toDto(ParticipationRequest request);

    default String mapRequestStatusToString(RequestStatus status) {
        return status != null ? status.name() : null;
    }

    default RequestStatus mapStringToRequestStatus(String status) {
        return status != null ? RequestStatus.valueOf(status) : null;
    }
}
