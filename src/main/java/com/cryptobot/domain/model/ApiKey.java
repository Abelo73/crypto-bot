package com.cryptobot.domain.model;

import com.cryptobot.adapter.ExchangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model representing encrypted exchange API credentials
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    private Long id;
    private Long userId;
    private ExchangeType exchangeType;
    private String apiKeyEncrypted;
    private String apiSecretEncrypted;
    private String label;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;

    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
}
