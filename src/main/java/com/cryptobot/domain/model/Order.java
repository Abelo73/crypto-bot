package com.cryptobot.domain.model;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.vo.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a trading order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private Long apiKeyId;
    private ExchangeType exchangeType;
    private String exchangeOrderId;
    private String symbol;
    private OrderType orderType;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderStatus status;
    private BigDecimal filledQuantity;
    private BigDecimal averagePrice;
    private TimeInForce timeInForce;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime exchangeCreatedAt;
    private LocalDateTime exchangeUpdatedAt;

    /**
     * Check if order is in a terminal state
     */
    public boolean isTerminal() {
        return status == OrderStatus.FILLED
                || status == OrderStatus.CANCELLED
                || status == OrderStatus.REJECTED
                || status == OrderStatus.EXPIRED;
    }

    /**
     * Check if order can be cancelled
     */
    public boolean isCancellable() {
        return status == OrderStatus.NEW || status == OrderStatus.PARTIALLY_FILLED;
    }

    /**
     * Update order status and filled quantity
     */
    public void updateStatus(OrderStatus newStatus, BigDecimal filledQty, BigDecimal avgPrice) {
        this.status = newStatus;
        this.filledQuantity = filledQty;
        this.averagePrice = avgPrice;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate remaining quantity
     */
    public BigDecimal getRemainingQuantity() {
        if (filledQuantity == null) {
            return quantity;
        }
        return quantity.subtract(filledQuantity);
    }

    /**
     * Check if order is a market order
     */
    public boolean isMarketOrder() {
        return orderType == OrderType.MARKET;
    }

    /**
     * Check if order is a limit order
     */
    public boolean isLimitOrder() {
        return orderType == OrderType.LIMIT;
    }
}
