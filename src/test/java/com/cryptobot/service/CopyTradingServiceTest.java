package com.cryptobot.service;

import com.cryptobot.domain.model.*;
import com.cryptobot.domain.vo.OrderSide;
import com.cryptobot.domain.vo.OrderType;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.repository.CopyRelationRepository;
import com.cryptobot.repository.entity.CopyRelationEntity;
import com.cryptobot.service.mapper.CopyRelationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyTradingServiceTest {

    @Mock
    private CopyRelationRepository copyRelationRepository;
    @Mock
    private CopyRelationMapper copyRelationMapper;
    @Mock
    private OrderService orderService;

    @InjectMocks
    private CopyTradingService copyTradingService;

    private Long leadUserId = 1L;
    private Long followerUserId = 2L;
    private Order leadOrder;

    @BeforeEach
    void setUp() {
        leadOrder = Order.builder()
                .userId(leadUserId)
                .symbol(new Symbol("BTCUSDT"))
                .side(OrderSide.BUY)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("1.0"))
                .build();
    }

    @Test
    void testMirrorTradeGeneratesScaledOrders() {
        // Arrange
        CopyRelationEntity entity = new CopyRelationEntity();
        entity.setFollowerUserId(followerUserId);
        entity.setLeadUserId(leadUserId);
        entity.setScaleFactor(new BigDecimal("0.5"));
        entity.setStatus(CopyTradingStatus.ACTIVE);

        CopyRelation domain = CopyRelation.builder()
                .followerUserId(followerUserId)
                .leadUserId(leadUserId)
                .scaleFactor(new BigDecimal("0.5"))
                .status(CopyTradingStatus.ACTIVE)
                .build();

        when(copyRelationRepository.findByLeadUserIdAndStatus(leadUserId, CopyTradingStatus.ACTIVE))
                .thenReturn(List.of(entity));
        when(copyRelationMapper.toDomain(entity)).thenReturn(domain);

        when(orderService.placeOrder(eq(followerUserId), any(Order.class)))
                .thenReturn(Mono.just(Order.builder().userId(followerUserId).build()));

        // Act
        copyTradingService.mirrorTrade(leadOrder);

        // Assert
        verify(orderService).placeOrder(eq(followerUserId),
                argThat(order -> order.getQuantity().compareTo(new BigDecimal("0.5")) == 0 &&
                        order.getSymbol().getValue().equals("BTCUSDT")));
    }

    @Test
    void testMirrorTradeNoFollowers() {
        // Arrange
        when(copyRelationRepository.findByLeadUserIdAndStatus(leadUserId, CopyTradingStatus.ACTIVE))
                .thenReturn(List.of());

        // Act
        copyTradingService.mirrorTrade(leadOrder);

        // Assert
        verifyNoInteractions(orderService);
    }
}
