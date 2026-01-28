package com.cryptobot.repository;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.repository.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {
    List<ApiKeyEntity> findByUserId(Long userId);

    List<ApiKeyEntity> findByUserIdAndActiveTrue(Long userId);

    Optional<ApiKeyEntity> findByUserIdAndExchangeTypeAndActiveTrue(Long userId, ExchangeType exchangeType);
}
