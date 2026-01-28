package com.cryptobot.domain.vo;

import lombok.Value;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a trading pair symbol (e.g., BTCUSDT)
 */
@Value
public class Symbol {
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9]{2,20}$");

    String value;

    public Symbol(String value) {
        Objects.requireNonNull(value, "Symbol cannot be null");

        String normalized = value.toUpperCase().trim();

        if (!SYMBOL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid symbol format: " + value + ". Must be 2-20 uppercase alphanumeric characters");
        }

        this.value = normalized;
    }

    public static Symbol of(String value) {
        return new Symbol(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
