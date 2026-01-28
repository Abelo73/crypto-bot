package com.cryptobot.repository.entity;

import com.cryptobot.adapter.ExchangeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balances", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "api_key_id", "asset" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "api_key_id", nullable = false)
    private Long apiKeyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", nullable = false)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String asset;

    @Column(name = "free_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal freeBalance;

    @Column(name = "locked_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal lockedBalance;

    @Column(name = "total_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal totalBalance;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
