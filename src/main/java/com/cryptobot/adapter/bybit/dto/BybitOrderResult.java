package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Bybit API v5 Order result
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitOrderResult {
    private String orderId;
    private String orderLinkId;
    private String symbol;
    private String price;
    private String qty;
    private String side;
    private String orderStatus;
    private String avgPrice;
    private String cumExecQty;
    private String cumExecValue;
    private String cumExecFee;
    private String orderType;
    private String timeInForce;
    private String createdTime; // unix timestamp in ms
    private String updatedTime; // unix timestamp in ms
}
