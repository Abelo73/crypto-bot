package com.cryptobot.api.controller;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.model.Balance;
import com.cryptobot.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/balances")
@RequiredArgsConstructor
@Tag(name = "Balance Management", description = "Endpoints for tracking exchange asset balances")
public class BalanceController {
    private final BalanceService balanceService;

    @PostMapping("/refresh")
    @Operation(summary = "Sync and refresh balances from exchange")
    public Mono<ResponseEntity<List<Balance>>> refreshBalances(
            @PathVariable Long userId,
            @RequestParam ExchangeType exchangeType) {
        return balanceService.refreshBalances(userId, exchangeType)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get cached balances (optional apiKeyId filter)")
    public ResponseEntity<List<Balance>> getBalances(
            @PathVariable Long userId,
            @RequestParam(required = false) Long apiKeyId) {
        return ResponseEntity.ok(balanceService.getCachedBalances(userId, apiKeyId));
    }
}
