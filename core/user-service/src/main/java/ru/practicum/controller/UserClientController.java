package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.UserShortDto;
import ru.practicum.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserClientController {

    private final UserService userService;

    @GetMapping
    public List<UserShortDto> getUsersByIds(@RequestParam List<Long> ids) {
        return userService.getUsersByIds(ids);
    }

    @GetMapping("/{userId}")
    public UserShortDto getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }
}
