package com.cryptobot.service;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeAdapterFactory;
import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.domain.model.Trade;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.repository.TradeRepository;
import com.cryptobot.repository.entity.TradeEntity;
import com.cryptobot.service.mapper.TradeMapper;
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
public class TradeService {
    private final TradeRepository tradeRepository;
    private final TradeMapper tradeMapper;
    private final ApiKeyService apiKeyService;
    private final ExchangeAdapterFactory adapterFactory;

    @Transactional
    public Mono<List<Trade>> syncTradeHistory(Long userId, ExchangeType exchangeType, String symbolValue, int limit) {
        ApiKey apiKeyModel = apiKeyService.getActiveKey(userId, exchangeType);
        ApiKeyService.Credentials credentials = apiKeyService.getDecryptedCredentials(apiKeyModel);
        ExchangeAdapter adapter = adapterFactory.getAdapter(exchangeType);
        Symbol symbol = new Symbol(symbolValue);

        return adapter.getExecutionHistory(credentials.apiKey(), credentials.apiSecret(), symbol, limit)
                .map(trade -> {
                    TradeEntity entity = tradeMapper.toEntity(trade);
                    entity.setUserId(userId);

                    // Prevent duplicates using unique constraint in DB
                    try {
                        return tradeRepository.save(entity);
                    } catch (Exception e) {
                        log.debug("Trade already exists: {}", trade.getExchangeTradeId());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .map(tradeMapper::toDomain)
                .collectList();
    }

    public List<Trade> getTrades(Long userId) {
        return tradeRepository.findByUserId(userId).stream()
                .map(tradeMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Trade> getTradesByOrder(Long orderId) {
        return tradeRepository.findByOrderId(orderId).stream()
                .map(tradeMapper::toDomain)
                .collect(Collectors.toList());
    }
}
