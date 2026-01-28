package com.cryptobot.service.task;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeAdapterFactory;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.repository.OrderRepository;
import com.cryptobot.repository.entity.OrderEntity;
import com.cryptobot.service.ApiKeyService;
import com.cryptobot.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background task to synchronize the status of open orders with the exchange.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSyncTask {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApiKeyService apiKeyService;
    private final ExchangeAdapterFactory adapterFactory;

    /**
     * Runs every minute to sync non-terminal orders.
     */
    @Scheduled(fixedRateString = "${app.sync.order-interval-ms:60000}")
    public void syncOpenOrders() {
        log.debug("Starting background order synchronization...");

        // Fetch all orders from DB that are not in a terminal state
        List<OrderEntity> openOrderEntities = orderRepository.findAll().stream()
                .filter(entity -> {
                    Order domain = orderMapper.toDomain(entity);
                    return !domain.isTerminal();
                })
                .toList();

        if (openOrderEntities.isEmpty()) {
            return;
        }

        log.info("Found {} open orders to synchronize", openOrderEntities.size());

        Flux.fromIterable(openOrderEntities)
                .flatMap(this::syncOrder)
                .subscribe(
                        syncedOrder -> log.debug("Synced order: {}", syncedOrder.getExchangeOrderId()),
                        error -> log.error("Unhandled error during background order sync: {}", error.getMessage()));
    }

    private Mono<Order> syncOrder(OrderEntity entity) {
        try {
            ApiKey apiKey = apiKeyService.getActiveKey(entity.getUserId(), entity.getExchangeType());
            ApiKeyService.Credentials credentials = apiKeyService.getDecryptedCredentials(apiKey);
            ExchangeAdapter adapter = adapterFactory.getAdapter(entity.getExchangeType());

            return adapter.getOrder(credentials.apiKey(), credentials.apiSecret(),
                    entity.getExchangeOrderId(), new Symbol(entity.getSymbol()))
                    .flatMap(updatedOrder -> {
                        // Update entity with new status and details
                        entity.setStatus(updatedOrder.getStatus());
                        entity.setFilledQuantity(updatedOrder.getFilledQuantity());
                        entity.setAveragePrice(updatedOrder.getAveragePrice());
                        entity.setUpdatedAt(LocalDateTime.now());
                        entity.setExchangeUpdatedAt(updatedOrder.getExchangeUpdatedAt());

                        orderRepository.save(entity);
                        return Mono.just(updatedOrder);
                    })
                    .onErrorResume(e -> {
                        log.warn("Could not sync order {}: {}", entity.getExchangeOrderId(), e.getMessage());
                        return Mono.empty();
                    });
        } catch (Exception e) {
            log.error("Fatal error syncing order {}: {}", entity.getId(), e.getMessage());
            return Mono.empty();
        }
    }
}
