package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.UserOperations;
import ru.practicum.dto.UserShortDto;
import ru.practicum.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserClientController implements UserOperations {

    private final UserService userService;

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return userService.getUsersByIds(ids);
    }

    @Override
    public UserShortDto getUserById(Long userId) {
        return userService.getUserById(userId);
    }
}
