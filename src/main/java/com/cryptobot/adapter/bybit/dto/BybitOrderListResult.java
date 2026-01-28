package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Bybit API v5 Order List Result (used for realtime queries)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitOrderListResult {
    private String category;
    private List<BybitOrderResult> list;
    private String nextCursor;
}
