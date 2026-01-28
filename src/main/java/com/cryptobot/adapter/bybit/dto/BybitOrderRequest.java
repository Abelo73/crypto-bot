package com.cryptobot.adapter.bybit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Bybit API v5 Order Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BybitOrderRequest {
    private String category; // spot, linear, inverse, option
    private String symbol;
    private String side; // Buy, Sell
    private String orderType; // Market, Limit
    private String qty;
    private String price;
    private Integer timeInForce; // GTC, IOC, FOK
    private String orderLinkId; // Client order ID
}
