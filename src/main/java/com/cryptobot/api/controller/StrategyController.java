package com.cryptobot.api.controller;

import com.cryptobot.domain.model.Strategy;
import com.cryptobot.domain.model.StrategyStatus;
import com.cryptobot.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to manage automated trading strategies
 */
@RestController
@RequestMapping("/api/users/{userId}/strategies")
@RequiredArgsConstructor
@Tag(name = "Strategy Management", description = "Endpoints for managing automated trading bots")
public class StrategyController {

    private final StrategyService strategyService;

    @PostMapping
    @Operation(summary = "Create a new automated trading strategy")
    public ResponseEntity<Strategy> createStrategy(
            @PathVariable Long userId,
            @RequestBody Strategy strategy) {
        strategy.setUserId(userId);
        return ResponseEntity.ok(strategyService.createStrategy(strategy));
    }

    @GetMapping
    @Operation(summary = "Get all strategies for a user")
    public ResponseEntity<List<Strategy>> getStrategies(@PathVariable Long userId) {
        return ResponseEntity.ok(strategyService.getStrategies(userId));
    }

    @PatchMapping("/{strategyId}/status")
    @Operation(summary = "Toggle strategy status (ACTIVE/PAUSED)")
    public ResponseEntity<Strategy> updateStatus(
            @PathVariable Long userId,
            @PathVariable Long strategyId,
            @RequestParam StrategyStatus status) {
        // Simple security check: verify user owns strategy
        Strategy existing = strategyService.getStrategy(strategyId);
        if (!existing.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(strategyService.updateStatus(strategyId, status));
    }
}
