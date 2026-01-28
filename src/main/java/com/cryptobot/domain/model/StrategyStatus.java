package com.cryptobot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status of an automated trading strategy
 */
@Getter
@AllArgsConstructor
public enum StrategyStatus {
    ACTIVE("Running and monitoring market updates"),
    PAUSED("Execution suspended by user"),
    COMPLETED("One-time execution finished"),
    FAILED("Terminated due to execution error");

    private final String description;
}
