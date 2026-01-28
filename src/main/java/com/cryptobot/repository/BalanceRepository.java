package com.cryptobot.repository;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.repository.entity.BalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<BalanceEntity, Long> {
    List<BalanceEntity> findByUserId(Long userId);

    List<BalanceEntity> findByUserIdAndApiKeyId(Long userId, Long apiKeyId);

    Optional<BalanceEntity> findByUserIdAndApiKeyIdAndAsset(Long userId, Long apiKeyId, String asset);
}
