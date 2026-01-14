package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/internal/users", fallback = UserClientFallback.class)
public interface UserClient extends UserOperations {
}
