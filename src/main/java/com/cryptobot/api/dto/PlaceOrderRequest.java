package com.cryptobot.api.dto;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.vo.OrderSide;
import com.cryptobot.domain.vo.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request to place a new order
 */
@Data
@Schema(description = "Request to place a new spot order")
public class PlaceOrderRequest {

    @NotNull(message = "Exchange type is required")
    private ExchangeType exchangeType;

    @NotBlank(message = "Symbol is required")
    @Schema(description = "Trading pair symbol", example = "BTCUSDT")
    private String symbol;

    @NotNull(message = "Order type is required")
    @Schema(description = "Type of order", example = "LIMIT")
    private OrderType orderType;

    @NotNull(message = "Order side is required")
    @Schema(description = "Order side", example = "BUY")
    private OrderSide side;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Amount of base asset to buy/sell", example = "0.001")
    private BigDecimal quantity;

    @Schema(description = "Price for LIMIT orders", example = "42000.50")
    private BigDecimal price;

    public void validate() {
        if (orderType == OrderType.LIMIT && price == null) {
            throw new IllegalArgumentException("Price is required for LIMIT orders");
        }
    }
}
