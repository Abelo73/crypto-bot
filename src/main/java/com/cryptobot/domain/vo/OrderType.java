package com.cryptobot.domain.vo;

/**
 * Order types supported by the platform
 */
public enum OrderType {
    /**
     * Market order - executes immediately at current market price
     */
    MARKET,

    /**
     * Limit order - executes only at specified price or better
     */
    LIMIT
}
