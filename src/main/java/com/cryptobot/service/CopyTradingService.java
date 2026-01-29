package com.cryptobot.service;

import com.cryptobot.domain.model.CopyRelation;
import com.cryptobot.domain.model.CopyTradingStatus;
import com.cryptobot.domain.model.Order;
import com.cryptobot.repository.CopyRelationRepository;
import com.cryptobot.service.mapper.CopyRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to manage copy-trading logic and trade mirroring.
 */
@Slf4j
@Service
public class CopyTradingService {

    private final CopyRelationRepository copyRelationRepository;
    private final CopyRelationMapper copyRelationMapper;
    private final OrderService orderService;

    public CopyTradingService(
            CopyRelationRepository copyRelationRepository,
            CopyRelationMapper copyRelationMapper,
            @Lazy OrderService orderService) {
        this.copyRelationRepository = copyRelationRepository;
        this.copyRelationMapper = copyRelationMapper;
        this.orderService = orderService;
    }

    /**
     * Mirror a lead trade to all active followers.
     * 
     * @param leadOrder The successful order placed by the lead trader
     */
    public void mirrorTrade(Order leadOrder) {
        log.info("Mirroring trade from lead user {} for symbol {}", leadOrder.getUserId(), leadOrder.getSymbol());

        List<CopyRelation> activeFollowers = copyRelationRepository
                .findByLeadUserIdAndStatus(leadOrder.getUserId(), CopyTradingStatus.ACTIVE)
                .stream()
                .map(copyRelationMapper::toDomain)
                .collect(Collectors.toList());

        if (activeFollowers.isEmpty()) {
            log.debug("No active followers found for lead user {}", leadOrder.getUserId());
            return;
        }

        Flux.fromIterable(activeFollowers)
                .flatMap(relation -> mirrorForFollower(relation, leadOrder))
                .subscribe(
                        result -> log.debug("Mirrored order placed for follower {}", result.getUserId()),
                        error -> log.error("Failed to mirror trade for some followers: {}", error.getMessage()));
    }

    private reactor.core.publisher.Mono<Order> mirrorForFollower(CopyRelation relation, Order leadOrder) {
        try {
            // Calculate scaled quantity
            BigDecimal followerQty = leadOrder.getQuantity().multiply(relation.getScaleFactor());

            log.info("Creating mirrored order for follower {}: Qty {} ({}x scale)",
                    relation.getFollowerUserId(), followerQty, relation.getScaleFactor());

            Order mirroredOrder = Order.builder()
                    .exchangeType(leadOrder.getExchangeType())
                    .symbol(leadOrder.getSymbol())
                    .side(leadOrder.getSide())
                    .orderType(leadOrder.getOrderType())
                    .quantity(followerQty)
                    .price(leadOrder.getPrice()) // Same price as lead for LIMIT orders
                    .build();

            return orderService.placeOrder(relation.getFollowerUserId(), mirroredOrder)
                    .doOnError(e -> log.error("Error placing mirrored order for follower {}: {}",
                            relation.getFollowerUserId(), e.getMessage()));
        } catch (Exception e) {
            log.error("Fatal error preparing mirrored order: {}", e.getMessage());
            return reactor.core.publisher.Mono.error(e);
        }
    }

    /**
     * Manual link creation for testing/admin purposes
     */
    public CopyRelation linkFollower(Long leadId, Long followerId, BigDecimal scale) {
        com.cryptobot.repository.entity.CopyRelationEntity entity = new com.cryptobot.repository.entity.CopyRelationEntity();
        entity.setLeadUserId(leadId);
        entity.setFollowerUserId(followerId);
        entity.setScaleFactor(scale);
        entity.setStatus(CopyTradingStatus.ACTIVE);
        return copyRelationMapper.toDomain(copyRelationRepository.save(entity));
    }
}
