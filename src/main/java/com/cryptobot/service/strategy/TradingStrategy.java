package com.cryptobot.service.strategy;

import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.model.Strategy;
import com.cryptobot.domain.model.TickerUpdate;

import java.util.Optional;

/**
 * Interface for automated trading strategy logic.
 */
public interface TradingStrategy {
    /**
     * Evaluates a market update to determine if any action should be taken.
     * 
     * @param strategy The current persistent state and configuration of the
     *                 strategy
     * @param ticker   The latest market data
     * @return An Optional Order if the strategy triggers a trade
     */
    Optional<Order> evaluate(Strategy strategy, TickerUpdate ticker);
}
