package com.cryptobot.service;

import com.cryptobot.domain.model.Strategy;
import com.cryptobot.domain.model.StrategyStatus;
import com.cryptobot.repository.StrategyRepository;
import com.cryptobot.repository.entity.StrategyEntity;
import com.cryptobot.service.mapper.StrategyMapper;
import com.cryptobot.service.strategy.StrategyExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to manage automated trading strategies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private final StrategyMapper strategyMapper;
    private final StrategyExecutor strategyExecutor;

    @Transactional
    public Strategy createStrategy(Strategy strategy) {
        log.info("Creating new strategy: {} for user {}", strategy.getName(), strategy.getUserId());
        strategy.setStatus(StrategyStatus.PAUSED); // Default to paused

        StrategyEntity entity = strategyMapper.toEntity(strategy);
        Strategy saved = strategyMapper.toDomain(strategyRepository.save(entity));

        return saved;
    }

    @Transactional
    public Strategy updateStatus(Long strategyId, StrategyStatus status) {
        StrategyEntity entity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + strategyId));

        log.info("Updating strategy {} status to {}", strategyId, status);
        entity.setStatus(status);
        Strategy updated = strategyMapper.toDomain(strategyRepository.save(entity));

        if (status == StrategyStatus.ACTIVE) {
            strategyExecutor.activateStrategy(updated);
        } else {
            strategyExecutor.deactivateStrategy(strategyId);
        }

        return updated;
    }

    public List<Strategy> getStrategies(Long userId) {
        return strategyRepository.findByUserId(userId).stream()
                .map(strategyMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Strategy getStrategy(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .map(strategyMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("Strategy not found: " + strategyId));
    }
}
