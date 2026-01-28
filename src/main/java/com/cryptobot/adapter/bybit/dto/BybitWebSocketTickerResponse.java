package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for Bybit V5 WebSocket ticker messages
 */
@Data
public class BybitWebSocketTickerResponse {
    private String topic;
    private String type;
    @JsonProperty("ts")
    private Long timestamp;
    private TickerData data;

    @Data
    public static class TickerData {
        private String symbol;
        @JsonProperty("lastPrice")
        private String lastPrice;
        @JsonProperty("highPrice24h")
        private String highPrice24h;
        @JsonProperty("lowPrice24h")
        private String lowPrice24h;
        @JsonProperty("prevPrice24h")
        private String prevPrice24h;
        @JsonProperty("volume24h")
        private String volume24h;
        @JsonProperty("turnover24h")
        private String turnover24h;
    }
}
