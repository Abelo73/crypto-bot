package com.cryptobot.domain.model;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.vo.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing an executed trade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private Long id;
    private Long orderId;
    private Long userId;
    private ExchangeType exchangeType;
    private String exchangeTradeId;
    private String symbol;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal commission;
    private String commissionAsset;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;

    /**
     * Calculate total value of the trade
     */
    public BigDecimal getTotalValue() {
        return quantity.multiply(price);
    }

    /**
     * Calculate net value after commission
     */
    public BigDecimal getNetValue() {
        BigDecimal total = getTotalValue();
        if (commission != null && commission.compareTo(BigDecimal.ZERO) > 0) {
            return total.subtract(commission);
        }
        return total;
    }
}
