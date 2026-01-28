package com.cryptobot.api.dto;

import com.cryptobot.adapter.ExchangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request to add a new exchange API key
 */
@Data
@Schema(description = "Request to add exchange API credentials")
public class AddApiKeyRequest {

    @NotNull(message = "Exchange type is required")
    @Schema(description = "The target exchange", example = "BYBIT")
    private ExchangeType exchangeType;

    @NotBlank(message = "API key is required")
    @Schema(description = "Exchange API Key", example = "your_api_key")
    private String apiKey;

    @NotBlank(message = "API secret is required")
    @Schema(description = "Exchange API Secret", example = "your_api_secret")
    private String apiSecret;

    @Schema(description = "Optional label for this key", example = "Main Trading Account")
    private String label;
}
