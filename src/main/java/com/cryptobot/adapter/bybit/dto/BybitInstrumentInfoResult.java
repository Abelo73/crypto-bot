package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Bybit API v5 Instruments Info result
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitInstrumentInfoResult {
    private String category;
    private List<BybitInstrumentInfo> list;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BybitInstrumentInfo {
        private String symbol;
        private String baseCoin;
        private String quoteCoin;
        private String showStatus; // Already Listed, etc.
        private LotSizeFilter lotSizeFilter;
        private PriceFilter priceFilter;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LotSizeFilter {
        private String basePrecision;
        private String quotePrecision;
        private String minOrderQty;
        private String maxOrderQty;
        private String minOrderAmt;
        private String maxOrderAmt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceFilter {
        private String tickSize;
    }
}
