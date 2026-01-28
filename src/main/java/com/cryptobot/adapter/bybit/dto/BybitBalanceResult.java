package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Bybit API v5 Wallet Balance Result
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitBalanceResult {
    private List<BybitBalanceAccount> list;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BybitBalanceAccount {
        private String accountType; // UNIFIED, SPOT, etc.
        private List<BybitCoinBalance> coin;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BybitCoinBalance {
        private String coin;
        private String equity;
        private String usdValue;
        private String walletBalance;
        private String free; // available to use
        private String locked; // frozen
    }
}
