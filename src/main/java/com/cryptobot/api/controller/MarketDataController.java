package com.cryptobot.api.controller;

import com.cryptobot.domain.model.TickerUpdate;
import com.cryptobot.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to expose real-time market data
 */
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Endpoints for real-time exchange market information")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get the latest ticker price from the WebSocket cache")
    public ResponseEntity<TickerUpdate> getLatestPrice(@PathVariable String symbol) {
        TickerUpdate ticker = marketDataService.getTicker(symbol.toUpperCase());
        if (ticker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticker);
    }
}
