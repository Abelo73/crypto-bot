package com.cryptobot.api.controller;

import com.cryptobot.domain.model.CopyRelation;
import com.cryptobot.domain.model.CopyTradingStatus;
import com.cryptobot.service.CopyTradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller to manage copy-trading relations
 */
@RestController
@RequestMapping("/api/users/{userId}/copy-trading")
@RequiredArgsConstructor
@Tag(name = "Copy Trading", description = "Endpoints for trade mirroring and social trading link management")
public class CopyTradingController {

    private final CopyTradingService copyTradingService;

    @PostMapping("/follow")
    @Operation(summary = "Follow a lead trader with a scaling factor")
    public ResponseEntity<CopyRelation> followLead(
            @PathVariable Long userId,
            @RequestParam Long leadUserId,
            @RequestParam(defaultValue = "1.0") BigDecimal scaleFactor) {
        return ResponseEntity.ok(copyTradingService.linkFollower(leadUserId, userId, scaleFactor));
    }
}
