package com.cryptobot.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Domain model for automated trading strategy configuration and current state
 */
@Data
@Builder
public class Strategy {
    private Long id;
    private Long userId;
    private Long apiKeyId;
    private String name;
    private StrategyType type;
    private StrategyStatus status;
    private String symbol;

    // Core parameters (e.g., baseAmount, priceThreshold)
    private Map<String, Object> parameters;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRunAt;
}
