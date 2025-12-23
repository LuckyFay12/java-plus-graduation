package ru.practicum.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> getAll(List<Long> ids, int from, int size);

    void delete(Long id);

    Optional<User> findById(Long userId);

    User getById(Long userId);

    List<UserShortDto> getUsersByIds(List<Long> ids);

    UserShortDto getUserById(Long userId);
}
