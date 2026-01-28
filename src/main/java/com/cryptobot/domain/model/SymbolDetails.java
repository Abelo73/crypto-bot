package com.cryptobot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Domain model for symbol-specific constraints and precision
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolDetails {
    private String symbol;
    private String baseAsset;
    private String quoteAsset;
    private int basePrecision;
    private int quotePrecision;
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private BigDecimal minAmount;
    private BigDecimal tickSize;
}
