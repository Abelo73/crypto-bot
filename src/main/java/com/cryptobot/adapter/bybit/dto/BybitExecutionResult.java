package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Bybit API v5 Execution (Trade) record
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitExecutionResult {
    private String symbol;
    private String orderId;
    private String side;
    private String execPrice;
    private String execQty;
    private String execFee;
    private String execId;
    private String execTime; // unix timestamp in ms
}
