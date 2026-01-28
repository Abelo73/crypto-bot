package com.cryptobot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Operational status of a copy-trading relationship
 */
@Getter
@AllArgsConstructor
public enum CopyTradingStatus {
    ACTIVE("Replicating lead trades"),
    PAUSED("Mirroring suspended"),
    ERROR("Stopped due to execution failure");

    private final String description;
}
