package com.cryptobot.api.controller;

import com.cryptobot.adapter.bybit.BybitAdapter;
import com.cryptobot.domain.model.Candle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controller for candlestick (OHLCV) market data
 */
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Endpoints for real-time exchange market information")
public class CandleController {

    private final BybitAdapter bybitAdapter;

    @GetMapping("/candles")
    @Operation(summary = "Get historical candlestick data for charting")
    public Mono<ResponseEntity<List<Candle>>> getCandles(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "5") String interval,
            @RequestParam(defaultValue = "200") int limit) {

        // Validate limit
        if (limit > 1000) {
            limit = 1000;
        }

        return bybitAdapter.getHistoricalCandles(symbol, interval, limit)
                .map(ResponseEntity::ok);
    }
}
