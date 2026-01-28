package com.cryptobot.repository;

import com.cryptobot.repository.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
    List<TradeEntity> findByUserId(Long userId);

    List<TradeEntity> findByOrderId(Long orderId);
}
