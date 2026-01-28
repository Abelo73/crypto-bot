package com.cryptobot.repository.entity;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.vo.OrderSide;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades", uniqueConstraints = @UniqueConstraint(columnNames = { "exchange_type", "exchange_trade_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", nullable = false)
    private ExchangeType exchangeType;

    @Column(name = "exchange_trade_id", nullable = false)
    private String exchangeTradeId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(precision = 20, scale = 8)
    private BigDecimal commission;

    @Column(name = "commission_asset")
    private String commissionAsset;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
