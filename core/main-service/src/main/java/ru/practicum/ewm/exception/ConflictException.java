package ru.practicum.ewm.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String entity, String constraint) {
        super(String.format("Integrity constraint violation for %s: %s", entity, constraint));
    }
}
