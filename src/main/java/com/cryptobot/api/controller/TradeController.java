package com.cryptobot.api.controller;

import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.domain.model.Trade;
import com.cryptobot.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/trades")
@RequiredArgsConstructor
@Tag(name = "Trade History", description = "Endpoints for viewing executed trade records")
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/sync")
    @Operation(summary = "Sync trade history from exchange")
    public Mono<ResponseEntity<List<Trade>>> syncTrades(
            @PathVariable Long userId,
            @RequestParam ExchangeType exchangeType,
            @RequestParam String symbol,
            @RequestParam(defaultValue = "50") int limit) {
        return tradeService.syncTradeHistory(userId, exchangeType, symbol, limit)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get user trade history")
    public ResponseEntity<List<Trade>> getTrades(@PathVariable Long userId) {
        return ResponseEntity.ok(tradeService.getTrades(userId));
    }
}
