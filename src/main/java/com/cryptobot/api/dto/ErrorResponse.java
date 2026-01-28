package com.cryptobot.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standardized error response structure
 */
@Data
@Builder
public class ErrorResponse {
    private String message;
    private String code;
    private LocalDateTime timestamp;
    private String path;
}
