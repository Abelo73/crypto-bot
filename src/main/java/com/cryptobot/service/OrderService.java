package com.cryptobot.service;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeAdapterFactory;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.model.SymbolDetails;
import com.cryptobot.domain.vo.OrderStatus;
import com.cryptobot.repository.OrderRepository;
import com.cryptobot.repository.entity.OrderEntity;
import com.cryptobot.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApiKeyService apiKeyService;
    private final ExchangeAdapterFactory adapterFactory;
    private final SymbolDetailsService symbolDetailsService;
    private final CopyTradingService copyTradingService;

    public OrderService(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            ApiKeyService apiKeyService,
            ExchangeAdapterFactory adapterFactory,
            SymbolDetailsService symbolDetailsService,
            @Lazy CopyTradingService copyTradingService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.apiKeyService = apiKeyService;
        this.adapterFactory = adapterFactory;
        this.symbolDetailsService = symbolDetailsService;
        this.copyTradingService = copyTradingService;
    }

    @Transactional
    public Mono<Order> placeOrder(Long userId, Order orderRequest) {
        ApiKey apiKeyModel = apiKeyService.getActiveKey(userId, orderRequest.getExchangeType());
        ApiKeyService.Credentials credentials = apiKeyService.getDecryptedCredentials(apiKeyModel);
        ExchangeAdapter adapter = adapterFactory.getAdapter(orderRequest.getExchangeType());

        // 1. Validate against symbol details
        SymbolDetails details = symbolDetailsService.getDetails(orderRequest.getExchangeType(),
                orderRequest.getSymbol());

        if (details != null) {
            validateAndNormalizeOrder(orderRequest, details);
        }

        // 2. Prepare domain model with required IDs
        orderRequest.setUserId(userId);
        orderRequest.setApiKeyId(apiKeyModel.getId());
        orderRequest.setStatus(OrderStatus.NEW);
        orderRequest.setCreatedAt(LocalDateTime.now());

        return adapter.placeOrder(credentials.apiKey(), credentials.apiSecret(), orderRequest)
                .flatMap(placedOrder -> {
                    // Update API key usage
                    apiKeyService.markAsUsed(apiKeyModel.getId());

                    // Save to database
                    OrderEntity entity = orderMapper.toEntity(placedOrder);
                    Order result = orderMapper.toDomain(orderRepository.save(entity));

                    // 3. Mirror trade to followers (async)
                    copyTradingService.mirrorTrade(result);

                    return Mono.just(result);
                })
                .doOnError(e -> log.error("Failed to place order: {}", e.getMessage()));
    }

    @Transactional
    public Mono<Order> cancelOrder(Long userId, Long orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderStatusException("Order not found: " + orderId));

        if (entity.getUserId() != userId) {
            throw new OrderStatusException("Unauthorized access to order");
        }

        Order domainOrder = orderMapper.toDomain(entity);
        if (!domainOrder.isCancellable()) {
            throw new OrderStatusException("Order is in terminal state and cannot be cancelled");
        }

        ApiKey apiKeyModel = apiKeyService.getActiveKey(userId, entity.getExchangeType());
        ApiKeyService.Credentials credentials = apiKeyService.getDecryptedCredentials(apiKeyModel);
        ExchangeAdapter adapter = adapterFactory.getAdapter(entity.getExchangeType());

        return adapter.cancelOrder(credentials.apiKey(), credentials.apiSecret(), domainOrder)
                .flatMap(cancelledOrder -> {
                    entity.setStatus(OrderStatus.CANCELLED);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return Mono.just(orderMapper.toDomain(orderRepository.save(entity)));
                });
    }

    private void validateAndNormalizeOrder(Order order, SymbolDetails details) {
        // Validate Min/Max Qty
        if (order.getQuantity().compareTo(details.getMinQuantity()) < 0) {
            throw new IllegalArgumentException("Quantity too small. Minimum is " + details.getMinQuantity());
        }
        if (details.getMaxQuantity() != null && order.getQuantity().compareTo(details.getMaxQuantity()) > 0) {
            throw new IllegalArgumentException("Quantity too large. Maximum is " + details.getMaxQuantity());
        }

        // Normalize precision
        order.setQuantity(order.getQuantity().setScale(details.getBasePrecision(), RoundingMode.DOWN));

        if (order.isLimitOrder() && order.getPrice() != null) {
            order.setPrice(order.getPrice().setScale(details.getQuotePrecision(), RoundingMode.HALF_UP));

            // Validate against tick size (simplified: just rounding to tick size)
            if (details.getTickSize() != null && details.getTickSize().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal roundedPrice = order.getPrice()
                        .divide(details.getTickSize(), 0, RoundingMode.HALF_UP)
                        .multiply(details.getTickSize());
                order.setPrice(roundedPrice);
            }
        }
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toDomain)
                .orElseThrow(() -> new OrderStatusException("Order not found: " + orderId));
    }

    private static class OrderStatusException extends RuntimeException {
        public OrderStatusException(String message) {
            super(message);
        }
    }
}
