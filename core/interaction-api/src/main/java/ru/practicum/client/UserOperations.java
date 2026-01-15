package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserShortDto;

import java.util.List;

public interface UserOperations {

    @GetMapping
    List<UserShortDto> getUsersByIds(@RequestParam List<Long> ids);

    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable Long userId);
}
