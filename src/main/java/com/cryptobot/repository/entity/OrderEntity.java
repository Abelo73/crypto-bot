package com.cryptobot.repository.entity;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.vo.OrderSide;
import com.cryptobot.domain.vo.OrderStatus;
import com.cryptobot.domain.vo.OrderType;
import com.cryptobot.domain.vo.TimeInForce;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
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

    @Column(name = "exchange_order_id")
    private String exchangeOrderId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "filled_quantity", precision = 20, scale = 8)
    private BigDecimal filledQuantity;

    @Column(name = "average_price", precision = 20, scale = 8)
    private BigDecimal averagePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_in_force")
    private TimeInForce timeInForce;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "exchange_created_at")
    private LocalDateTime exchangeCreatedAt;

    @Column(name = "exchange_updated_at")
    private LocalDateTime exchangeUpdatedAt;
}
