package ru.practicum.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String error, Object value) {
        super(String.format("Поле: %s. Ошибка: %s. Значение: %s", field, error, value));
    }
}