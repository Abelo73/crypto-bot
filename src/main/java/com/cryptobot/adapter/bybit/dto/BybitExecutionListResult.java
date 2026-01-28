package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Bybit API v5 Execution (Trade) List Result
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitExecutionListResult {
    private String category;
    private List<BybitExecutionResult> list;
}
