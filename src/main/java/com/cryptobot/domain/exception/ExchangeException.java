package com.cryptobot.domain.exception;

/**
 * Thrown when an error occurs while communicating with the exchange
 */
public class ExchangeException extends RuntimeException {
    private final String errorCode;

    public ExchangeException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public ExchangeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExchangeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INTERNAL_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
