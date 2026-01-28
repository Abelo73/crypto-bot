package com.cryptobot.domain.exception;

/**
 * Thrown when an API key is not found or is invalid
 */
public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException(String message) {
        super(message);
    }
}
