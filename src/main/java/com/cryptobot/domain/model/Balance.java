package com.cryptobot.domain.model;

import com.cryptobot.adapter.ExchangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing asset balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance {
    private Long id;
    private Long userId;
    private Long apiKeyId;
    private ExchangeType exchangeType;
    private String asset;
    private BigDecimal freeBalance;
    private BigDecimal lockedBalance;
    private BigDecimal totalBalance;
    private LocalDateTime updatedAt;

    /**
     * Check if there's sufficient free balance
     */
    public boolean hasSufficientBalance(BigDecimal required) {
        return freeBalance != null && freeBalance.compareTo(required) >= 0;
    }

    /**
     * Update balance values
     */
    public void updateBalances(BigDecimal free, BigDecimal locked) {
        this.freeBalance = free;
        this.lockedBalance = locked;
        this.totalBalance = free.add(locked);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if balance is zero
     */
    public boolean isZero() {
        return totalBalance == null || totalBalance.compareTo(BigDecimal.ZERO) == 0;
    }
}
