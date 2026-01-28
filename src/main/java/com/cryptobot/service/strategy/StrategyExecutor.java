package com.cryptobot.service.strategy;

import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.model.Strategy;
import com.cryptobot.domain.model.StrategyStatus;
import com.cryptobot.domain.model.TickerUpdate;
import com.cryptobot.repository.StrategyRepository;
import com.cryptobot.repository.entity.StrategyEntity;
import com.cryptobot.service.MarketDataService;
import com.cryptobot.service.OrderService;
import com.cryptobot.service.mapper.StrategyMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance execution engine for automated strategies.
 * Listens to market data and dispatches updates to active strategy logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyExecutor {

    private final MarketDataService marketDataService;
    private final StrategyRepository strategyRepository;
    private final OrderService orderService;
    private final Map<String, TradingStrategy> strategyRegistry;
    private final StrategyMapper strategyMapper;

    // Map of active strategies in memory (id -> Strategy)
    private final Map<Long, Strategy> activeStrategies = new ConcurrentHashMap<>();

    private Disposable marketStreamSubscription;

    @PostConstruct
    public void start() {
        log.info("Initializing Strategy Execution Engine...");
        loadActiveStrategies();
        subscribeToMarketData();
    }

    @PreDestroy
    public void stop() {
        if (marketStreamSubscription != null) {
            marketStreamSubscription.dispose();
        }
    }

    private void loadActiveStrategies() {
        strategyRepository.findByStatus(StrategyStatus.ACTIVE).forEach(entity -> {
            log.info("Loading active strategy: {} [{}]", entity.getName(), entity.getType());
            activeStrategies.put(entity.getId(), strategyMapper.toDomain(entity));
        });
    }

    private void subscribeToMarketData() {
        marketStreamSubscription = marketDataService.getTickerStream()
                .subscribe(this::onPriceUpdate);
    }

    private void onPriceUpdate(TickerUpdate ticker) {
        // Dispatch to all active strategies watching this symbol
        activeStrategies.values().stream()
                .filter(s -> s.getSymbol().equalsIgnoreCase(ticker.getSymbol()))
                .forEach(s -> evaluateStrategy(s, ticker));
    }

    private void evaluateStrategy(Strategy strategy, TickerUpdate ticker) {
        try {
            TradingStrategy logic = strategyRegistry.get(strategy.getType().name());
            if (logic == null) {
                log.error("No implementation found for strategy type: {}", strategy.getType());
                return;
            }

            logic.evaluate(strategy, ticker).ifPresent(order -> {
                executeOrder(strategy, order);
            });
        } catch (Exception e) {
            log.error("Error evaluating strategy {}: {}", strategy.getId(), e.getMessage());
        }
    }

    private void executeOrder(Strategy strategy, Order order) {
        log.info("Order triggered by Strategy [{}]: {}", strategy.getName(), order);

        // Ensure the order has correctly mapped exchange context
        order.setUserId(strategy.getUserId());
        order.setApiKeyId(strategy.getApiKeyId());

        orderService.placeOrder(order.getUserId(), order)
                .doOnSuccess(placed -> {
                    strategy.setLastRunAt(LocalDateTime.now());
                    updateStrategyState(strategy);
                })
                .subscribe();
    }

    private void updateStrategyState(Strategy strategy) {
        // Sync back to DB
        strategyRepository.findById(strategy.getId()).ifPresent(entity -> {
            entity.setLastRunAt(strategy.getLastRunAt());
            strategyRepository.save(entity);
        });
    }

    public void activateStrategy(Strategy strategy) {
        activeStrategies.put(strategy.getId(), strategy);
        log.info("Strategy activated: {}", strategy.getName());
    }

    public void deactivateStrategy(Long strategyId) {
        activeStrategies.remove(strategyId);
        log.info("Strategy deactivated: {}", strategyId);
    }
}
