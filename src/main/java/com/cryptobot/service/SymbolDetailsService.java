package com.cryptobot.service;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeAdapterFactory;
import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.model.SymbolDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to cache and provide symbol details (precision, min/max quantity) for
 * validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolDetailsService {

    private final ExchangeAdapterFactory adapterFactory;

    // Cache: ExchangeType -> Map<Symbol, SymbolDetails>
    private final Map<ExchangeType, Map<String, SymbolDetails>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * Refresh the symbol cache every 24 hours.
     */
    @Scheduled(fixedRate = 86400000)
    public void refreshCache() {
        log.info("Refreshing symbol details cache...");

        for (ExchangeType type : ExchangeType.values()) {
            try {
                ExchangeAdapter adapter = adapterFactory.getAdapter(type);
                adapter.getSymbolDetails()
                        .doOnSuccess(details -> {
                            Map<String, SymbolDetails> symbolMap = details.stream()
                                    .collect(Collectors.toMap(SymbolDetails::getSymbol, Function.identity()));
                            cache.put(type, symbolMap);
                            log.info("Cached {} symbols for {}", symbolMap.size(), type);
                        })
                        .subscribe(null, error -> log.error("Failed to refresh symbol cache for {}: {}", type,
                                error.getMessage()));
            } catch (Exception e) {
                log.warn("Could not load symbol details for {}: {}", type, e.getMessage());
            }
        }
    }

    public SymbolDetails getDetails(ExchangeType exchangeType, String symbol) {
        Map<String, SymbolDetails> symbolMap = cache.getOrDefault(exchangeType, Collections.emptyMap());
        return symbolMap.get(symbol);
    }
}
