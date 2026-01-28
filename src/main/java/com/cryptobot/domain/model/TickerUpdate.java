package com.cryptobot.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain model for a real-time price ticker update
 */
@Value
@Builder
public class TickerUpdate {
    String symbol;
    BigDecimal lastPrice;
    BigDecimal highPrice24h;
    BigDecimal lowPrice24h;
    BigDecimal volume24h;
    Instant timestamp;
}
