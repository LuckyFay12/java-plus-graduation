package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.entity.Hit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {

    @Mapping(target = "timestamp", source = "createdAt")
    EndpointHit toDto(Hit hit);

    @Mapping(target = "createdAt", source = "timestamp")
    Hit toEntity(EndpointHit dto);

}
