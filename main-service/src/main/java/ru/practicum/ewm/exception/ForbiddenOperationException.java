package ru.practicum.ewm.exception;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException(String operation, String reason) {
        super(String.format("Cannot %s because: %s", operation, reason));
    }
}
