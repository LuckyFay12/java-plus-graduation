package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.event.LocationDto;
import ru.practicum.ewm.entity.event.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}
