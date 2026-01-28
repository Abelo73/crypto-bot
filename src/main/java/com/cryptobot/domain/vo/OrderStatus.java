package com.cryptobot.domain.vo;

/**
 * Order status throughout its lifecycle
 */
public enum OrderStatus {
    /**
     * Order has been created and submitted to exchange
     */
    NEW,

    /**
     * Order is partially filled
     */
    PARTIALLY_FILLED,

    /**
     * Order is completely filled
     */
    FILLED,

    /**
     * Order has been cancelled
     */
    CANCELLED,

    /**
     * Order was rejected by the exchange
     */
    REJECTED,

    /**
     * Order has expired (for time-limited orders)
     */
    EXPIRED
}
