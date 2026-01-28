package com.cryptobot.adapter;

import com.cryptobot.domain.model.Balance;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.Symbol;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Strategic interface for exchange-agnostic operations.
 * All exchange integrations must implement this interface.
 */
public interface ExchangeAdapter {

    /**
     * Get the exchange type this adapter handles
     */
    ExchangeType getExchangeType();

    /**
     * Get account balances for the provided credentials
     */
    Mono<List<Balance>> getBalances(String apiKey, String apiSecret);

    /**
     * Place a new order on the exchange
     */
    Mono<Order> placeOrder(String apiKey, String apiSecret, Order order);

    /**
     * Cancel an existing order
     */
    Mono<Order> cancelOrder(String apiKey, String apiSecret, Order order);

    /**
     * Get current status of an order
     */
    Mono<Order> getOrder(String apiKey, String apiSecret, String exchangeOrderId, Symbol symbol);

    /**
     * Get recent order history
     */
    Flux<Order> getOrderHistory(String apiKey, String apiSecret, Symbol symbol, int limit);

    /**
     * Get recent trade execution history
     */
    Flux<com.cryptobot.domain.model.Trade> getExecutionHistory(String apiKey, String apiSecret, Symbol symbol,
            int limit);

    /**
     * Get details for all symbols (category filter applied by adapter)
     */
    Mono<List<com.cryptobot.domain.model.SymbolDetails>> getSymbolDetails();
}
