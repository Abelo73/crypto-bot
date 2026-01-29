package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Bybit API v5 Kline (Candlestick) Result
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitKlineResult {
    private String symbol;
    private String category;
    private List<List<String>> list; // Each item: [startTime, openPrice, highPrice, lowPrice, closePrice, volume,
                                     // turnover]
}
