package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient extends RequestOperations {
}
