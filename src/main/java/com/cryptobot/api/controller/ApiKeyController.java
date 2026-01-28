package com.cryptobot.api.controller;

import com.cryptobot.api.dto.AddApiKeyRequest;
import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Key Management", description = "Endpoints for managing exchange API credentials")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @PostMapping
    @Operation(summary = "Add a new exchange API key")
    public ResponseEntity<ApiKey> addApiKey(
            @PathVariable Long userId,
            @Valid @RequestBody AddApiKeyRequest request) {
        ApiKey apiKey = apiKeyService.addApiKey(
                userId,
                request.getExchangeType(),
                request.getApiKey(),
                request.getApiSecret(),
                request.getLabel());
        return ResponseEntity.ok(apiKey);
    }

    @GetMapping
    @Operation(summary = "List all API keys for a user")
    public ResponseEntity<List<ApiKey>> listApiKeys(@PathVariable Long userId) {
        return ResponseEntity.ok(apiKeyService.getUserApiKeys(userId));
    }
}
