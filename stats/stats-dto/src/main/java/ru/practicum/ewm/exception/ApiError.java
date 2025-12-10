package ru.practicum.ewm.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ApiError {
    private int code;
    private String error;
}
