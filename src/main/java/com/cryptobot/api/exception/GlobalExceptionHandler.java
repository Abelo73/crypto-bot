package com.cryptobot.api.exception;

import com.cryptobot.api.dto.ErrorResponse;
import com.cryptobot.domain.exception.ApiKeyNotFoundException;
import com.cryptobot.domain.exception.ExchangeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExchangeException.class)
    public ResponseEntity<ErrorResponse> handleExchangeException(ExchangeException e, HttpServletRequest request) {
        log.error("Exchange error: {} - Code: {}", e.getMessage(), e.getErrorCode());
        return createErrorResponse(e.getMessage(), e.getErrorCode(), HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyNotFound(ApiKeyNotFoundException e, HttpServletRequest request) {
        return createErrorResponse(e.getMessage(), "API_KEY_NOT_FOUND", HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e,
            HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return createErrorResponse("Validation failed: " + errors, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return createErrorResponse(e.getMessage(), "INVALID_ARGUMENT", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception: ", e);
        return createErrorResponse("An unexpected error occurred", "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(String message, String code, HttpStatus status,
            HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .code(code)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
