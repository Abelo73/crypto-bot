package com.cryptobot.service;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.exception.ApiKeyNotFoundException;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.repository.ApiKeyRepository;
import com.cryptobot.repository.entity.ApiKeyEntity;
import com.cryptobot.security.EncryptionService;
import com.cryptobot.service.mapper.ApiKeyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final EncryptionService encryptionService;

    @Transactional
    public ApiKey addApiKey(Long userId, ExchangeType exchangeType, String apiKey, String apiSecret, String label) {
        String encryptedKey = encryptionService.encrypt(apiKey);
        String encryptedSecret = encryptionService.encrypt(apiSecret);

        ApiKeyEntity entity = ApiKeyEntity.builder()
                .userId(userId)
                .exchangeType(exchangeType)
                .apiKeyEncrypted(encryptedKey)
                .apiSecretEncrypted(encryptedSecret)
                .label(label)
                .active(true)
                .build();

        return apiKeyMapper.toDomain(apiKeyRepository.save(entity));
    }

    public List<ApiKey> getUserApiKeys(Long userId) {
        return apiKeyRepository.findByUserId(userId).stream()
                .map(apiKeyMapper::toDomain)
                .collect(Collectors.toList());
    }

    public ApiKey getActiveKey(Long userId, ExchangeType exchangeType) {
        return apiKeyRepository.findByUserIdAndExchangeTypeAndActiveTrue(userId, exchangeType)
                .map(apiKeyMapper::toDomain)
                .orElseThrow(
                        () -> new ApiKeyNotFoundException("No active API key found for exchange: " + exchangeType));
    }

    @Transactional
    public void markAsUsed(Long id) {
        apiKeyRepository.findById(id).ifPresent(entity -> {
            entity.setLastUsedAt(LocalDateTime.now());
            apiKeyRepository.save(entity);
        });
    }

    /**
     * Decrypts credentials for internal use (e.g., calling exchange API)
     */
    public Credentials getDecryptedCredentials(ApiKey apiKey) {
        return new Credentials(
                encryptionService.decrypt(apiKey.getApiKeyEncrypted()),
                encryptionService.decrypt(apiKey.getApiSecretEncrypted()));
    }

    public record Credentials(String apiKey, String apiSecret) {
    }
}
