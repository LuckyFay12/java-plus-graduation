package ru.practicum.utils;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public abstract class ResponseGenerator {

    protected final ResponseEntity<Object> makeResult(Object object, HttpStatusCode code) {
        return ResponseEntity.status(code).body(object);
    }
}
