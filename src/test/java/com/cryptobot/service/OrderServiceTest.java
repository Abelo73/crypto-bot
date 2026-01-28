package com.cryptobot.service;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeAdapterFactory;
import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.OrderSide;
import com.cryptobot.domain.vo.OrderStatus;
import com.cryptobot.domain.vo.OrderType;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.repository.OrderRepository;
import com.cryptobot.repository.entity.OrderEntity;
import com.cryptobot.service.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private ExchangeAdapterFactory adapterFactory;
    @Mock
    private ExchangeAdapter exchangeAdapter;
    @Mock
    private SymbolDetailsService symbolDetailsService;

    @InjectMocks
    private OrderService orderService;

    private Long userId = 1L;
    private Order orderRequest;
    private ApiKey apiKey;

    @BeforeEach
    void setUp() {
        orderRequest = Order.builder()
                .exchangeType(ExchangeType.BYBIT)
                .symbol(new Symbol("BTCUSDT"))
                .orderType(OrderType.LIMIT)
                .side(OrderSide.BUY)
                .quantity(new BigDecimal("0.001"))
                .price(new BigDecimal("40000"))
                .build();

        apiKey = ApiKey.builder()
                .id(100L)
                .userId(userId)
                .exchangeType(ExchangeType.BYBIT)
                .apiKeyEncrypted("encrypted-key")
                .apiSecretEncrypted("encrypted-secret")
                .active(true)
                .build();
    }

    @Test
    void testPlaceOrderSuccess() {
        // Arrange
        when(apiKeyService.getActiveKey(userId, ExchangeType.BYBIT)).thenReturn(apiKey);
        when(apiKeyService.getDecryptedCredentials(apiKey))
                .thenReturn(new ApiKeyService.Credentials("plain-key", "plain-secret"));
        when(adapterFactory.getAdapter(ExchangeType.BYBIT)).thenReturn(exchangeAdapter);

        Order placedOrder = Order.builder()
                .exchangeOrderId("order-123")
                .status(OrderStatus.NEW)
                .build();

        when(exchangeAdapter.placeOrder(anyString(), anyString(), any(Order.class)))
                .thenReturn(Mono.just(placedOrder));

        OrderEntity orderEntity = new OrderEntity();
        when(orderMapper.toEntity(any(Order.class))).thenReturn(orderEntity);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderMapper.toDomain(any(OrderEntity.class))).thenReturn(placedOrder);

        // Act & Assert
        StepVerifier.create(orderService.placeOrder(userId, orderRequest))
                .expectNextMatches(order -> "order-123".equals(order.getExchangeOrderId()))
                .verifyComplete();

        verify(apiKeyService).markAsUsed(apiKey.getId());
        verify(orderRepository).save(any(OrderEntity.class));
    }
}
