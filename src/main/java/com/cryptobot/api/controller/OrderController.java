package com.cryptobot.api.controller;

import com.cryptobot.api.dto.PlaceOrderRequest;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for placing and managing trading orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new trading order")
    public Mono<ResponseEntity<Order>> placeOrder(
            @PathVariable Long userId,
            @Valid @RequestBody PlaceOrderRequest request) {
        request.validate();

        Order orderRequest = Order.builder()
                .exchangeType(request.getExchangeType())
                .symbol(new Symbol(request.getSymbol()))
                .orderType(request.getOrderType())
                .side(request.getSide())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build();

        return orderService.placeOrder(userId, orderRequest)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel an open order")
    public Mono<ResponseEntity<Order>> cancelOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        return orderService.cancelOrder(userId, orderId)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get user order history")
    public ResponseEntity<List<Order>> getOrderHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get single order details")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }
}
