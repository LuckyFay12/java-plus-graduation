package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import ru.practicum.dto.UserShortDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserClientFallback implements UserClient {
    @Override
    public UserShortDto getUserById(Long userId) {
        log.warn("Fallback: User service недоступен для userId: {}", userId);
        return UserShortDto.builder()
                .id(userId)
                .name("FALLBACK User #" + userId)
                .build();
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        log.warn("Fallback: User service недоступен для ids: {}", ids);
        return ids.stream()
                .map(id -> UserShortDto.builder()
                        .id(id)
                        .name("FALLBACK User #" + id)
                        .build())
                .collect(Collectors.toList());
    }
}