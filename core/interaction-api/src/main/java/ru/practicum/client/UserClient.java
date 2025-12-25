package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/users", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping
    List<UserShortDto> getUsersByIds(@RequestParam List<Long> ids);

    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable Long userId);
}
