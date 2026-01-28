package com.cryptobot.repository;

import com.cryptobot.domain.model.StrategyStatus;
import com.cryptobot.repository.entity.StrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing automated trading strategies
 */
@Repository
public interface StrategyRepository extends JpaRepository<StrategyEntity, Long> {
    List<StrategyEntity> findByUserId(Long userId);

    List<StrategyEntity> findByStatus(StrategyStatus status);
}
