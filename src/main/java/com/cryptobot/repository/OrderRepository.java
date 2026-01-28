package com.cryptobot.repository;

import com.cryptobot.repository.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findByUserIdAndSymbol(Long userId, String symbol);

    Optional<OrderEntity> findByExchangeOrderId(String exchangeOrderId);
}
