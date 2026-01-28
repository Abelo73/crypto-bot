package com.cryptobot.adapter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for retrieving the correct ExchangeAdapter for an exchange type
 */
@Component
public class ExchangeAdapterFactory {

    private final Map<ExchangeType, ExchangeAdapter> adapters;

    public ExchangeAdapterFactory(List<ExchangeAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(ExchangeAdapter::getExchangeType, Function.identity()));
    }

    /**
     * Get adapter for the specified exchange type
     * 
     * @param type The exchange type
     * @return The corresponding adapter
     * @throws IllegalArgumentException if no adapter found for the type
     */
    public ExchangeAdapter getAdapter(ExchangeType type) {
        ExchangeAdapter adapter = adapters.get(type);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter found for exchange type: " + type);
        }
        return adapter;
    }
}
