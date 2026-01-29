package com.cryptobot.adapter.bybit;

import com.cryptobot.adapter.ExchangeAdapter;
import com.cryptobot.adapter.ExchangeType;
import com.cryptobot.adapter.bybit.dto.*;
import com.cryptobot.domain.model.Balance;
import com.cryptobot.domain.model.Candle;
import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.model.Trade;
import com.cryptobot.domain.model.SymbolDetails;
import com.cryptobot.domain.vo.OrderType;
import com.cryptobot.domain.vo.OrderStatus;
import com.cryptobot.domain.vo.OrderSide;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.domain.exception.ExchangeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bybit implementation of ExchangeAdapter using V5 REST API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BybitAdapter implements ExchangeAdapter {

        private final WebClient.Builder webClientBuilder;
        private final ObjectMapper objectMapper;

        @Value("${bybit.api.base-url:https://api-testnet.bybit.com}")
        private String baseUrl;

        @Override
        public ExchangeType getExchangeType() {
                return ExchangeType.BYBIT;
        }

        @Override
        public Mono<List<Balance>> getBalances(String apiKey, String apiSecret) {
                return makeSignedRequest(apiKey, apiSecret, "GET", "/v5/account/wallet-balance",
                                Map.of("accountType", "UNIFIED"))
                                .map(response -> {
                                        try {
                                                BybitResponse<BybitBalanceResult> bybitResponse = objectMapper
                                                                .readValue(response,
                                                                                objectMapper.getTypeFactory()
                                                                                                .constructParametricType(
                                                                                                                BybitResponse.class,
                                                                                                                BybitBalanceResult.class));

                                                if (bybitResponse.getRetCode() != 0) {
                                                        throw new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg());
                                                }

                                                if (bybitResponse.getResult().getList().isEmpty()) {
                                                        return Collections.emptyList();
                                                }

                                                return bybitResponse.getResult().getList().get(0).getCoin().stream()
                                                                .map(coin -> Balance.builder()
                                                                                .exchangeType(ExchangeType.BYBIT)
                                                                                .asset(coin.getCoin())
                                                                                .freeBalance(new BigDecimal(
                                                                                                coin.getFree()))
                                                                                .lockedBalance(new BigDecimal(
                                                                                                coin.getLocked()))
                                                                                .totalBalance(new BigDecimal(coin
                                                                                                .getWalletBalance()))
                                                                                .updatedAt(LocalDateTime.now())
                                                                                .build())
                                                                .collect(Collectors.toList());

                                        } catch (Exception e) {
                                                throw new ExchangeException("Failed to list balances", e);
                                        }
                                });
        }

        @Override
        public Mono<Order> placeOrder(String apiKey, String apiSecret, Order order) {
                Map<String, Object> params = new HashMap<>();
                params.put("category", "spot");
                params.put("symbol", order.getSymbol());
                params.put("side", order.getSide().toString());
                params.put("orderType", order.getOrderType().toString());
                params.put("qty", order.getQuantity().toPlainString());

                if (order.getOrderType() == OrderType.LIMIT) {
                        params.put("price", order.getPrice().toPlainString());
                }

                return makeSignedRequest(apiKey, apiSecret, "POST", "/v5/order/create", params)
                                .map(response -> {
                                        try {
                                                BybitResponse<BybitOrderResult> bybitResponse = objectMapper.readValue(
                                                                response,
                                                                objectMapper.getTypeFactory().constructParametricType(
                                                                                BybitResponse.class,
                                                                                BybitOrderResult.class));
                                                if (bybitResponse.getRetCode() != 0) {
                                                        throw new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg());
                                                }
                                                return mapBybitOrderToDomain(bybitResponse.getResult(), order);
                                        } catch (Exception e) {
                                                throw new ExchangeException("Failed to place order", e);
                                        }
                                });
        }

        @Override
        public Mono<Order> cancelOrder(String apiKey, String apiSecret, Order order) {
                return makeSignedRequest(apiKey, apiSecret, "POST", "/v5/order/cancel", Map.of(
                                "category", "spot",
                                "symbol", order.getSymbol(),
                                "orderId", order.getExchangeOrderId())).map(response -> {
                                        try {
                                                BybitResponse<BybitOrderResult> bybitResponse = objectMapper.readValue(
                                                                response,
                                                                objectMapper.getTypeFactory().constructParametricType(
                                                                                BybitResponse.class,
                                                                                BybitOrderResult.class));
                                                if (bybitResponse.getRetCode() != 0) {
                                                        throw new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg());
                                                }
                                                return Order.builder()
                                                                .exchangeOrderId(order.getExchangeOrderId())
                                                                .symbol(order.getSymbol())
                                                                .status(OrderStatus.CANCELLED)
                                                                .build();
                                        } catch (Exception e) {
                                                throw new ExchangeException("Failed to cancel order", e);
                                        }
                                });
        }

        @Override
        public Mono<Order> getOrder(String apiKey, String apiSecret, String exchangeOrderId, Symbol symbol) {
                return makeSignedRequest(apiKey, apiSecret, "GET", "/v5/order/realtime", Map.of(
                                "category", "spot",
                                "symbol", symbol.getValue(),
                                "orderId", exchangeOrderId)).map(response -> {
                                        try {
                                                BybitResponse<BybitOrderListResult> bybitResponse = objectMapper
                                                                .readValue(response,
                                                                                objectMapper.getTypeFactory()
                                                                                                .constructParametricType(
                                                                                                                BybitResponse.class,
                                                                                                                BybitOrderListResult.class));
                                                if (bybitResponse.getRetCode() != 0) {
                                                        throw new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg());
                                                }
                                                if (bybitResponse.getResult() == null
                                                                || bybitResponse.getResult().getList() == null
                                                                || bybitResponse.getResult().getList().isEmpty()) {
                                                        throw new ExchangeException("Order not found on exchange: "
                                                                        + exchangeOrderId);
                                                }
                                                BybitOrderResult bybitOrder = bybitResponse.getResult().getList()
                                                                .get(0);
                                                return mapBybitOrderToDomain(bybitOrder,
                                                                Order.builder().exchangeOrderId(exchangeOrderId)
                                                                                .symbol(symbol.getValue()).build());
                                        } catch (Exception e) {
                                                throw new ExchangeException("Failed to get order", e);
                                        }
                                });
        }

        @Override
        public Flux<Order> getOrderHistory(String apiKey, String apiSecret, Symbol symbol, int limit) {
                // Bybit V5 has a unified history endpoint for both active and historical
                // orders.
                // For simplicity, this example will just return empty.
                // A full implementation would query /v5/order/history and map results.
                return Flux.empty();
        }

        @Override
        public Flux<Trade> getExecutionHistory(String apiKey, String apiSecret, Symbol symbol, int limit) {
                return makeSignedRequest(apiKey, apiSecret, "GET", "/v5/execution/list", Map.of(
                                "category", "spot",
                                "symbol", symbol.getValue(),
                                "limit", String.valueOf(limit))).flatMapMany(response -> {
                                        try {
                                                BybitResponse<BybitExecutionListResult> bybitResponse = objectMapper
                                                                .readValue(response,
                                                                                objectMapper.getTypeFactory()
                                                                                                .constructParametricType(
                                                                                                                BybitResponse.class,
                                                                                                                BybitExecutionListResult.class));
                                                if (bybitResponse.getRetCode() != 0) {
                                                        return Mono.error(new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg()));
                                                }
                                                if (bybitResponse.getResult() == null
                                                                || bybitResponse.getResult().getList() == null) {
                                                        return Flux.empty();
                                                }
                                                return Flux.fromIterable(bybitResponse.getResult().getList())
                                                                .map(exec -> Trade.builder()
                                                                                .exchangeType(ExchangeType.BYBIT)
                                                                                .exchangeTradeId(exec.getExecId())
                                                                                .symbol(exec.getSymbol())
                                                                                .side(OrderSide.valueOf(exec.getSide()
                                                                                                .toUpperCase()))
                                                                                .quantity(new BigDecimal(
                                                                                                exec.getExecQty()))
                                                                                .price(new BigDecimal(
                                                                                                exec.getExecPrice()))
                                                                                .commission(new BigDecimal(
                                                                                                exec.getExecFee()))
                                                                                .executedAt(LocalDateTime.ofInstant(
                                                                                                Instant.ofEpochMilli(
                                                                                                                Long.parseLong(exec
                                                                                                                                .getExecTime())),
                                                                                                ZoneId.systemDefault()))
                                                                                .build());
                                        } catch (Exception e) {
                                                return Mono.error(new ExchangeException(
                                                                "Failed to get execution history", e));
                                        }
                                });
        }

        @Override
        public Mono<List<SymbolDetails>> getSymbolDetails() {
                return webClientBuilder.build()
                                .get()
                                .uri(baseUrl + "/v5/market/instruments-info?category=spot")
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(response -> {
                                        try {
                                                BybitResponse<BybitInstrumentInfoResult> bybitResponse = objectMapper
                                                                .readValue(response,
                                                                                objectMapper.getTypeFactory()
                                                                                                .constructParametricType(
                                                                                                                BybitResponse.class,
                                                                                                                BybitInstrumentInfoResult.class));
                                                if (bybitResponse.getRetCode() != 0) {
                                                        throw new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg());
                                                }
                                                if (bybitResponse.getResult() == null
                                                                || bybitResponse.getResult().getList() == null) {
                                                        return Collections.emptyList();
                                                }
                                                return bybitResponse.getResult().getList().stream()
                                                                .map(info -> SymbolDetails.builder()
                                                                                .symbol(info.getSymbol())
                                                                                .baseAsset(info.getBaseCoin())
                                                                                .quoteAsset(info.getQuoteCoin())
                                                                                .basePrecision(info
                                                                                                .getLotSizeFilter() != null
                                                                                                                ? new BigDecimal(
                                                                                                                                info.getLotSizeFilter()
                                                                                                                                                .getBasePrecision())
                                                                                                                                .scale()
                                                                                                                : 8)
                                                                                .quotePrecision(info
                                                                                                .getLotSizeFilter() != null
                                                                                                                ? new BigDecimal(
                                                                                                                                info.getLotSizeFilter()
                                                                                                                                                .getQuotePrecision())
                                                                                                                                .scale()
                                                                                                                : 8)
                                                                                .minQuantity(info
                                                                                                .getLotSizeFilter() != null
                                                                                                                ? new BigDecimal(
                                                                                                                                info.getLotSizeFilter()
                                                                                                                                                .getMinOrderQty())
                                                                                                                : BigDecimal.ZERO)
                                                                                .maxQuantity(info
                                                                                                .getLotSizeFilter() != null
                                                                                                                ? new BigDecimal(
                                                                                                                                info.getLotSizeFilter()
                                                                                                                                                .getMaxOrderQty())
                                                                                                                : new BigDecimal(
                                                                                                                                "9999999999"))
                                                                                .minAmount(info.getLotSizeFilter() != null
                                                                                                ? new BigDecimal(info
                                                                                                                .getLotSizeFilter()
                                                                                                                .getMinOrderAmt())
                                                                                                : BigDecimal.ZERO)
                                                                                .tickSize(info.getPriceFilter() != null
                                                                                                ? new BigDecimal(info
                                                                                                                .getPriceFilter()
                                                                                                                .getTickSize())
                                                                                                : new BigDecimal(
                                                                                                                "0.00000001"))
                                                                                .build())
                                                                .collect(Collectors.toList());
                                        } catch (Exception e) {
                                                throw new ExchangeException("Failed to get symbol details", e);
                                        }
                                });
        }

        /**
         * Get historical candlestick data for charting
         * 
         * @param symbol   Trading pair symbol (e.g., BTCUSDT)
         * @param interval Time interval: 1, 3, 5, 15, 30, 60, 120, 240, 360, 720, D, W,
         *                 M
         * @param limit    Number of candles to fetch (max 1000, default 200)
         * @return Flux of Candle objects
         */
        public Mono<List<Candle>> getHistoricalCandles(String symbol, String interval, int limit) {
                return webClientBuilder.build()
                                .get()
                                .uri(baseUrl + "/v5/market/kline?category=spot&symbol=" + symbol
                                                + "&interval=" + interval + "&limit=" + limit)
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(response -> {
                                        try {
                                                BybitResponse<BybitKlineResult> bybitResponse = objectMapper
                                                                .readValue(response,
                                                                                objectMapper.getTypeFactory()
                                                                                                .constructParametricType(
                                                                                                                BybitResponse.class,
                                                                                                                BybitKlineResult.class));
                                                if (bybitResponse.getRetCode() != 0) {
                                                        throw new ExchangeException("Bybit API error: "
                                                                        + bybitResponse.getRetMsg());
                                                }
                                                if (bybitResponse.getResult() == null
                                                                || bybitResponse.getResult().getList() == null) {
                                                        return Collections.emptyList();
                                                }

                                                // Map Bybit kline data to Candle domain model
                                                return bybitResponse.getResult().getList().stream()
                                                                .map(kline -> Candle.builder()
                                                                                .symbol(symbol)
                                                                                .interval(interval)
                                                                                .openTime(Long.parseLong(kline.get(0)))
                                                                                .open(new BigDecimal(kline.get(1)))
                                                                                .high(new BigDecimal(kline.get(2)))
                                                                                .low(new BigDecimal(kline.get(3)))
                                                                                .close(new BigDecimal(kline.get(4)))
                                                                                .volume(new BigDecimal(kline.get(5)))
                                                                                .build())
                                                                .collect(Collectors.toList());
                                        } catch (Exception e) {
                                                throw new ExchangeException("Failed to get candlestick data", e);
                                        }
                                });
        }

        private Mono<String> makeSignedRequest(String apiKey, String apiSecret, String method, String path,
                        Map<String, Object> params) {
                long timestamp = System.currentTimeMillis();
                String recvWindow = "5000";
                String queryString = "";
                String jsonBody = "";

                if ("GET".equals(method)) {
                        queryString = params.entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .map(e -> e.getKey() + "=" + e.getValue())
                                        .collect(Collectors.joining("&"));
                } else {
                        try {
                                jsonBody = objectMapper.writeValueAsString(params);
                        } catch (Exception e) {
                                return Mono.error(new RuntimeException("Failed to serialize params"));
                        }
                }

                String signaturePayload = timestamp + apiKey + recvWindow
                                + (method.equals("GET") ? queryString : jsonBody);
                String signature = generateSignature(signaturePayload, apiSecret);

                String finalUrl = baseUrl + path + (queryString.isEmpty() ? "" : "?" + queryString);

                WebClient.RequestBodySpec requestSpec = webClientBuilder.build()
                                .method(org.springframework.http.HttpMethod.valueOf(method))
                                .uri(finalUrl)
                                .header("X-BAPI-API-KEY", apiKey)
                                .header("X-BAPI-TIMESTAMP", String.valueOf(timestamp))
                                .header("X-BAPI-RECV-WINDOW", recvWindow)
                                .header("X-BAPI-SIGN", signature);

                if (!"GET".equals(method)) {
                        requestSpec.bodyValue(jsonBody);
                        requestSpec.header("Content-Type", "application/json");
                }

                return requestSpec.retrieve()
                                .bodyToMono(String.class)
                                .doOnNext(res -> log.debug("Bybit Response: {}", res));
        }

        private String generateSignature(String payload, String secret) {
                try {
                        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                                        "HmacSHA256");
                        sha256_HMAC.init(secret_key);
                        return bytesToHex(sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
                } catch (Exception e) {
                        throw new RuntimeException("Failed to generate signature", e);
                }
        }

        private String bytesToHex(byte[] bytes) {
                StringBuilder result = new StringBuilder();
                for (byte b : bytes) {
                        result.append(String.format("%02x", b));
                }
                return result.toString();
        }

        private OrderStatus mapStatus(String bybitStatus) {
                return switch (bybitStatus.toUpperCase()) {
                        case "NEW" -> OrderStatus.NEW;
                        case "PARTIALLYFILLED", "PARTIALLY_FILLED" -> OrderStatus.PARTIALLY_FILLED;
                        case "FILLED" -> OrderStatus.FILLED;
                        case "CANCELLED", "CANCELED", "DEACTIVATED" -> OrderStatus.CANCELLED;
                        case "REJECTED" -> OrderStatus.REJECTED;
                        default -> OrderStatus.NEW;
                };
        }

        private Order mapBybitOrderToDomain(BybitOrderResult result, Order original) {
                return Order.builder()
                                .id(original.getId())
                                .userId(original.getUserId())
                                .apiKeyId(original.getApiKeyId())
                                .exchangeType(ExchangeType.BYBIT)
                                .exchangeOrderId(result.getOrderId())
                                .symbol(result.getSymbol())
                                .side(OrderSide.valueOf(result.getSide().toUpperCase()))
                                .orderType(OrderType.valueOf(result.getOrderType().toUpperCase()))
                                .quantity(new BigDecimal(result.getQty()))
                                .price(result.getPrice() != null && !result.getPrice().isEmpty()
                                                ? new BigDecimal(result.getPrice())
                                                : null)
                                .status(mapStatus(result.getOrderStatus()))
                                .filledQuantity(new BigDecimal(result.getCumExecQty()))
                                .averagePrice(result.getAvgPrice() != null && !result.getAvgPrice().isEmpty()
                                                ? new BigDecimal(result.getAvgPrice())
                                                : BigDecimal.ZERO)
                                .createdAt(original.getCreatedAt())
                                .exchangeCreatedAt(LocalDateTime.ofInstant(
                                                Instant.ofEpochMilli(Long.parseLong(result.getCreatedTime())),
                                                ZoneId.systemDefault()))
                                .exchangeUpdatedAt(LocalDateTime.ofInstant(
                                                Instant.ofEpochMilli(Long.parseLong(result.getUpdatedTime())),
                                                ZoneId.systemDefault()))
                                .build();
        }
}
