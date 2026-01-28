package com.cryptobot.service.strategy.impl;

import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.model.Strategy;
import com.cryptobot.domain.model.TickerUpdate;
import com.cryptobot.domain.vo.*;
import com.cryptobot.service.strategy.TradingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Concrete implementation of Dollar Cost Averaging strategy.
 * Triggers a BUY order at fixed intervals.
 */
@Slf4j
@Component("DCA")
public class DcaStrategy implements TradingStrategy {

    @Override
    public Optional<Order> evaluate(Strategy strategy, TickerUpdate ticker) {
        if (!shouldRun(strategy)) {
            return Optional.empty();
        }

        // Extract parameters
        BigDecimal amountUsdt = new BigDecimal(strategy.getParameters().getOrDefault("amountUsdt", "10").toString());

        // Calculate quantity based on current price
        BigDecimal quantity = amountUsdt.divide(ticker.getLastPrice(), 8, BigDecimal.ROUND_DOWN);

        log.info("DCA Strategy [{}] triggered for {}. Order amount: {} USDT ({} units)",
                strategy.getName(), strategy.getSymbol(), amountUsdt, quantity);

        return Optional.of(Order.builder()
                .exchangeType(strategy.getApiKeyId() != null ? null : null) // Will be filled by executor
                .symbol(new Symbol(strategy.getSymbol()))
                .side(OrderSide.BUY)
                .orderType(OrderType.MARKET)
                .quantity(quantity)
                .build());
    }

    private boolean shouldRun(Strategy strategy) {
        LocalDateTime lastRun = strategy.getLastRunAt();
        if (lastRun == null) {
            return true;
        }

        long intervalMinutes = Long
                .parseLong(strategy.getParameters().getOrDefault("intervalMinutes", "1440").toString());
        return ChronoUnit.MINUTES.between(lastRun, LocalDateTime.now()) >= intervalMinutes;
    }
}
