package com.cryptobot.adapter.bybit;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.adapter.bybit.dto.*;
import com.cryptobot.domain.exception.ExchangeException;
import com.cryptobot.domain.model.Balance;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bybit implementation of ExchangeAdapter using V5 REST API
 */
@Slf4j
@Service
public class BybitAdapter implements ExchangeAdapter {

    private final WebClient webClient;
    private final BybitSignatureGenerator signatureGenerator;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public BybitAdapter(
            WebClient.Builder webClientBuilder,
            BybitSignatureGenerator signatureGenerator,
            ObjectMapper objectMapper,
            @Value("${bybit.testnet.base-url:https://api-testnet.bybit.com}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.signatureGenerator = signatureGenerator;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    @Override
    public ExchangeType getExchangeType() {
        return ExchangeType.BYBIT;
    }

    @Override
    public Mono<List<Balance>> getBalances(String apiKey, String apiSecret) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String recvWindow = "5000";
        String queryString = "accountType=SPOT";
        String signature = signatureGenerator.generate(apiSecret, timestamp, apiKey, recvWindow, queryString);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v5/account/wallet-balance")
                        .queryParam("accountType", "SPOT")
                        .build())
                .header("X-BAPI-API-KEY", apiKey)
                .header("X-BAPI-TIMESTAMP", timestamp)
                .header("X-BAPI-RECV-WINDOW", recvWindow)
                .header("X-BAPI-SIGN", signature)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BybitResponse<BybitBalanceResult>>() {
                })
                .retryWhen(retryStrategy())
                .map(response -> {
                    validateResponse(response);
                    return response.getResult().getList().stream()
                            .flatMap(account -> account.getCoin().stream())
                            .map(coin -> Balance.builder()
                                    .exchangeType(ExchangeType.BYBIT)
                                    .asset(coin.getCoin())
                                    .freeBalance(new BigDecimal(coin.getFree()))
                                    .lockedBalance(new BigDecimal(coin.getLocked()))
                                    .totalBalance(new BigDecimal(coin.getWalletBalance()))
                                    .updatedAt(LocalDateTime.now())
                                    .build())
                            .collect(Collectors.toList());
                });
    }

    @Override
    public Mono<Order> placeOrder(String apiKey, String apiSecret, Order order) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String recvWindow = "5000";

        BybitOrderRequest request = BybitOrderRequest.builder()
                .category("spot")
                .symbol(order.getSymbol().getValue())
                .side(capitalizeFirst(order.getSide().name()))
                .orderType(capitalizeFirst(order.getOrderType().name()))
                .qty(order.getQuantity().toPlainString())
                .price(order.isLimitOrder() ? order.getPrice().toPlainString() : null)
                .timeInForce(1) // GTC is usually 1 or "GTC" string in v5, but check docs
                .build();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            return Mono.error(new ExchangeException("Failed to serialize order request", e));
        }

        String signature = signatureGenerator.generate(apiSecret, timestamp, apiKey, recvWindow, payload);

        return webClient.post()
                .uri("/v5/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-BAPI-API-KEY", apiKey)
                .header("X-BAPI-TIMESTAMP", timestamp)
                .header("X-BAPI-RECV-WINDOW", recvWindow)
                .header("X-BAPI-SIGN", signature)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BybitResponse<BybitOrderResult>>() {
                })
                .retryWhen(retryStrategy())
                .map(response -> {
                    validateResponse(response);
                    return mapToDomain(response.getResult(), order);
                });
    }

    @Override
    public Mono<Order> cancelOrder(String apiKey, String apiSecret, Order order) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String recvWindow = "5000";
        String payload = String.format("{\"category\":\"spot\",\"symbol\":\"%s\",\"orderId\":\"%s\"}",
                order.getSymbol().getValue(), order.getExchangeOrderId());

        String signature = signatureGenerator.generate(apiSecret, timestamp, apiKey, recvWindow, payload);

        return webClient.post()
                .uri("/v5/order/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-BAPI-API-KEY", apiKey)
                .header("X-BAPI-TIMESTAMP", timestamp)
                .header("X-BAPI-RECV-WINDOW", recvWindow)
                .header("X-BAPI-SIGN", signature)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BybitResponse<BybitOrderResult>>() {
                })
                .retryWhen(retryStrategy())
                .map(response -> {
                    validateResponse(response);
                    return mapToDomain(response.getResult(), order);
                });
    }

    @Override
    public Mono<Order> getOrder(String apiKey, String apiSecret, String exchangeOrderId, Symbol symbol) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String recvWindow = "5000";
        String queryString = String.format("category=spot&symbol=%s&orderId=%s", symbol.getValue(), exchangeOrderId);
        String signature = signatureGenerator.generate(apiSecret, timestamp, apiKey, recvWindow, queryString);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v5/order/realtime")
                        .queryParam("category", "spot")
                        .queryParam("symbol", symbol.getValue())
                        .queryParam("orderId", exchangeOrderId)
                        .build())
                .header("X-BAPI-API-KEY", apiKey)
                .header("X-BAPI-TIMESTAMP", timestamp)
                .header("X-BAPI-RECV-WINDOW", recvWindow)
                .header("X-BAPI-SIGN", signature)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BybitResponse<BybitOrderListResult>>() {
                })
                .retryWhen(retryStrategy())
                .map(response -> {
                    validateResponse(response);
                    BybitOrderListResult result = response.getResult();
                    if (result == null || result.getList() == null || result.getList().isEmpty()) {
                        throw new ExchangeException("Order not found on exchange: " + exchangeOrderId);
                    }
                    // For single order query, we take the first match
                    BybitOrderResult bybitOrder = result.getList().get(0);
                    return mapToDomain(bybitOrder, Order.builder()
                            .exchangeOrderId(exchangeOrderId)
                            .symbol(symbol)
                            .build());
                });
    }

    @Override
    public Flux<Order> getOrderHistory(String apiKey, String apiSecret, Symbol symbol, int limit) {
        // Implementation for history...
        return Flux.empty();
    }

    @Override
    public Flux<com.cryptobot.domain.model.Trade> getExecutionHistory(String apiKey, String apiSecret, Symbol symbol,
            int limit) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String recvWindow = "5000";
        String queryString = String.format("category=spot&symbol=%s&limit=%d", symbol.getValue(), limit);
        String signature = signatureGenerator.generate(apiSecret, timestamp, apiKey, recvWindow, queryString);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v5/execution/list")
                        .queryParam("category", "spot")
                        .queryParam("symbol", symbol.getValue())
                        .queryParam("limit", limit)
                        .build())
                .header("X-BAPI-API-KEY", apiKey)
                .header("X-BAPI-TIMESTAMP", timestamp)
                .header("X-BAPI-RECV-WINDOW", recvWindow)
                .header("X-BAPI-SIGN", signature)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BybitResponse<BybitExecutionListResult>>() {
                })
                .retryWhen(retryStrategy())
                .flatMapMany(response -> {
                    validateResponse(response);
                    return Flux.fromIterable(
                            response.getResult().getList() != null ? response.getResult().getList() : List.of());
                })
                .map(exec -> com.cryptobot.domain.model.Trade.builder()
                        .exchangeType(ExchangeType.BYBIT)
                        .exchangeTradeId(exec.getExecId())
                        .symbol(exec.getSymbol())
                        .side(OrderSide.valueOf(exec.getSide().toUpperCase()))
                        .quantity(new BigDecimal(exec.getExecQty()))
                        .price(new BigDecimal(exec.getExecPrice()))
                        .commission(new BigDecimal(exec.getExecFee()))
                        .executedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(exec.getExecTime())),
                                ZoneId.systemDefault()))
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    @Override
    public Mono<List<com.cryptobot.domain.model.SymbolDetails>> getSymbolDetails() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v5/market/instruments-info")
                        .queryParam("category", "spot")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BybitResponse<BybitInstrumentInfoResult>>() {
                })
                .retryWhen(retryStrategy())
                .map(response -> {
                    validateResponse(response);
                    return response.getResult().getList().stream()
                            .map(info -> com.cryptobot.domain.model.SymbolDetails.builder()
                                    .symbol(info.getSymbol())
                                    .baseAsset(info.getBaseCoin())
                                    .quoteAsset(info.getQuoteCoin())
                                    .basePrecision(info.getLotSizeFilter() != null
                                            ? new BigDecimal(info.getLotSizeFilter().getBasePrecision()).scale()
                                            : 8)
                                    .quotePrecision(info.getLotSizeFilter() != null
                                            ? new BigDecimal(info.getLotSizeFilter().getQuotePrecision()).scale()
                                            : 8)
                                    .minQuantity(info.getLotSizeFilter() != null
                                            ? new BigDecimal(info.getLotSizeFilter().getMinOrderQty())
                                            : BigDecimal.ZERO)
                                    .maxQuantity(info.getLotSizeFilter() != null
                                            ? new BigDecimal(info.getLotSizeFilter().getMaxOrderQty())
                                            : new BigDecimal("9999999999"))
                                    .minAmount(info.getLotSizeFilter() != null
                                            ? new BigDecimal(info.getLotSizeFilter().getMinOrderAmt())
                                            : BigDecimal.ZERO)
                                    .tickSize(info.getPriceFilter() != null
                                            ? new BigDecimal(info.getPriceFilter().getTickSize())
                                            : new BigDecimal("0.00000001"))
                                    .build())
                            .collect(Collectors.toList());
                });
    }

    private void validateResponse(BybitResponse<?> response) {
        if (response.getRetCode() != 0) {
            throw new ExchangeException("Bybit API error: " + response.getRetMsg(),
                    String.valueOf(response.getRetCode()));
        }
    }

    private Retry retryStrategy() {
        return Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> !(throwable instanceof ExchangeException)) // Don't retry logic errors
                .doBeforeRetry(retrySignal -> log.warn("Retrying Bybit request (attempt {})...",
                        retrySignal.totalRetries() + 1));
    }

    private Order mapToDomain(BybitOrderResult result, Order original) {
        return Order.builder()
                .id(original.getId())
                .userId(original.getUserId())
                .apiKeyId(original.getApiKeyId())
                .exchangeType(ExchangeType.BYBIT)
                .exchangeOrderId(result.getOrderId())
                .symbol(new Symbol(result.getSymbol()))
                .side(OrderSide.valueOf(result.getSide().toUpperCase()))
                .orderType(OrderType.valueOf(result.getOrderType().toUpperCase()))
                .quantity(new BigDecimal(result.getQty()))
                .price(result.getPrice() != null ? new BigDecimal(result.getPrice()) : null)
                .status(mapStatus(result.getOrderStatus()))
                .filledQuantity(new BigDecimal(result.getCumExecQty()))
                .averagePrice(new BigDecimal(result.getAvgPrice()))
                .createdAt(original.getCreatedAt())
                .exchangeCreatedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(Long.parseLong(result.getCreatedTime())), ZoneId.systemDefault()))
                .exchangeUpdatedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(Long.parseLong(result.getUpdatedTime())), ZoneId.systemDefault()))
                .build();
    }

    private OrderStatus mapStatus(String bybitStatus) {
        return switch (bybitStatus.toUpperCase()) {
            case "NEW" -> OrderStatus.NEW;
            case "PARTIALLYFILLED" -> OrderStatus.PARTIALLY_FILLED;
            case "FILLED" -> OrderStatus.FILLED;
            case "CANCELLED", "DEACTIVATED" -> OrderStatus.CANCELLED;
            case "REJECTED" -> OrderStatus.REJECTED;
            default -> OrderStatus.NEW;
        };
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
