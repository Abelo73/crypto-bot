package com.cryptobot.service;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeAdapterFactory;
import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.domain.model.Balance;
import com.cryptobot.repository.BalanceRepository;
import com.cryptobot.repository.entity.BalanceEntity;
import com.cryptobot.service.mapper.BalanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {
    private final BalanceRepository balanceRepository;
    private final BalanceMapper balanceMapper;
    private final ApiKeyService apiKeyService;
    private final ExchangeAdapterFactory adapterFactory;

    @Transactional
    public Mono<List<Balance>> refreshBalances(Long userId, ExchangeType exchangeType) {
        ApiKey apiKeyModel = apiKeyService.getActiveKey(userId, exchangeType);
        ApiKeyService.Credentials credentials = apiKeyService.getDecryptedCredentials(apiKeyModel);
        ExchangeAdapter adapter = adapterFactory.getAdapter(exchangeType);

        return adapter.getBalances(credentials.apiKey(), credentials.apiSecret())
                .map(balances -> {
                    // Sync with database
                    List<BalanceEntity> entities = balances.stream()
                            .map(b -> {
                                BalanceEntity entity = balanceMapper.toEntity(b);
                                entity.setUserId(userId);
                                entity.setApiKeyId(apiKeyModel.getId());

                                // Check if exists to update or create
                                balanceRepository
                                        .findByUserIdAndApiKeyIdAndAsset(userId, apiKeyModel.getId(), b.getAsset())
                                        .ifPresent(existing -> entity.setId(existing.getId()));

                                return balanceRepository.save(entity);
                            })
                            .collect(Collectors.toList());

                    return entities.stream().map(balanceMapper::toDomain).collect(Collectors.toList());
                });
    }

    public List<Balance> getCachedBalances(Long userId, Long apiKeyId) {
        List<BalanceEntity> entities;
        if (apiKeyId != null) {
            entities = balanceRepository.findByUserIdAndApiKeyId(userId, apiKeyId);
        } else {
            entities = balanceRepository.findByUserId(userId);
        }

        return entities.stream()
                .map(balanceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
