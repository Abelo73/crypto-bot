package com.cryptobot.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model for a copy-trading relationship between a lead and follower
 */
@Data
@Builder
public class CopyRelation {
    private Long id;
    private Long leadUserId;
    private Long followerUserId;
    private BigDecimal scaleFactor;
    private CopyTradingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
