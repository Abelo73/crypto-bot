package com.cryptobot.domain.vo;

/**
 * Time in force for orders
 */
public enum TimeInForce {
    /**
     * Good Till Cancel - order remains active until filled or cancelled
     */
    GTC,

    /**
     * Immediate Or Cancel - fill immediately or cancel unfilled portion
     */
    IOC,

    /**
     * Fill Or Kill - fill entire order immediately or cancel completely
     */
    FOK
}
