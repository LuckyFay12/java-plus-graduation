package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.entity.user.User;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.repository.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email {} уже существует", request.getEmail());
        }
        User user = userMapper.toEntity(request);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findByIdIn(ids, pageable);
        } else {
            users = userRepository.findAll(pageable).getContent();
        }
        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User getById(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));
    }
}
