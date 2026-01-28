package com.cryptobot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supported automated trading strategy types
 */
@Getter
@AllArgsConstructor
public enum StrategyType {
    DCA("Dollar Cost Averaging"),
    GRID("Grid Trading");

    private final String description;
}
