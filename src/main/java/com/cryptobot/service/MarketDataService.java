package com.cryptobot.service;

import com.cryptobot.adapter.bybit.dto.BybitWebSocketTickerResponse;
import com.cryptobot.domain.model.TickerUpdate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service to manage WebSocket connections for real-time market data.
 * Maintains an in-memory cache of the latest ticker prices.
 * Falls back to REST polling if WebSocket is unstable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final ObjectMapper objectMapper;
    private final WebSocketClient webSocketClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${bybit.websocket.base-url:wss://stream-testnet.bybit.com/v5/public/spot}")
    private String wsUrl;

    @Value("${bybit.api.base-url:https://api-testnet.bybit.com}")
    private String restfulUrl;

    @Value("${bybit.websocket.topics:ticker.BTCUSDT,ticker.ETHUSDT,ticker.SOLUSDT,ticker.BNBUSDT,ticker.XRPUSDT}")
    private List<String> topics;

    private final Map<String, TickerUpdate> tickerCache = new ConcurrentHashMap<>();

    // Sink to broadcast ticker updates to other internal services
    private final Sinks.Many<TickerUpdate> tickerSink = Sinks.many().multicast().directBestEffort();

    private Disposable connectionDisposable;
    private Disposable pollingDisposable;

    @PostConstruct
    public void init() {
        // 1. Initial snapshot via REST to prevent 404s
        fetchInitialSnapshot();

        // 2. Start WebSocket
        connectWs();

        // 3. Start Polling as backup (every 10s)
        startPolling();
    }

    @PreDestroy
    public void cleanup() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
        if (pollingDisposable != null && !pollingDisposable.isDisposed()) {
            pollingDisposable.dispose();
        }
    }

    private void fetchInitialSnapshot() {
        log.info("Fetching initial market data snapshot via REST...");
        WebClient client = webClientBuilder.baseUrl(restfulUrl).build();

        // Fetch each symbol individually to safely handle potential API quirks
        Flux.fromIterable(topics)
                .map(t -> t.replace("ticker.", ""))
                .flatMap(symbol -> client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v5/market/tickers")
                                .queryParam("category", "spot")
                                .queryParam("symbol", symbol)
                                .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .onErrorResume(e -> {
                            log.error("Failed to fetch initial snapshot for {}: {}", symbol, e.getMessage());
                            return Mono.empty();
                        }))
                .subscribe(this::processRestResponse);
    }

    private void startPolling() {
        pollingDisposable = Flux.interval(Duration.ofSeconds(10))
                .flatMap(i -> Flux.fromIterable(topics))
                .map(t -> t.replace("ticker.", ""))
                .flatMap(symbol -> {
                    WebClient client = webClientBuilder.baseUrl(restfulUrl).build();
                    return client.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/v5/market/tickers")
                                    .queryParam("category", "spot")
                                    .queryParam("symbol", symbol)
                                    .build())
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .onErrorResume(e -> Mono.empty());
                })
                .subscribe(this::processRestResponse);
    }

    private void processRestResponse(JsonNode root) {
        try {
            if (root.has("result") && root.get("result").has("list")) {
                JsonNode list = root.get("result").get("list");
                if (list.isArray()) {
                    for (JsonNode item : list) {
                        TickerUpdate update = TickerUpdate.builder()
                                .symbol(item.get("symbol").asText())
                                .lastPrice(new BigDecimal(item.get("lastPrice").asText()))
                                .highPrice24h(new BigDecimal(item.get("highPrice24h").asText()))
                                .lowPrice24h(new BigDecimal(item.get("lowPrice24h").asText()))
                                .volume24h(new BigDecimal(item.get("volume24h").asText()))
                                .timestamp(Instant.now())
                                .build();

                        tickerCache.put(update.getSymbol(), update);
                        tickerSink.tryEmitNext(update);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing REST response: {}", e.getMessage());
        }
    }

    /**
     * Connect to the WebSocket and subscribe to topics.
     */
    public void connectWs() {
        log.info("Connecting to Bybit WebSocket: {}", wsUrl);

        connectionDisposable = webSocketClient.execute(URI.create(wsUrl), session -> {
            log.info("WebSocket session established: {}", session.getId());

            // Sending subscription message
            String subMessage = String.format("{\"op\": \"subscribe\", \"args\": %s}",
                    toJsonArray(topics));

            Mono<Void> sendSub = session.send(Flux.just(session.textMessage(subMessage)));

            Mono<Void> receiveMessages = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(this::processWsMessage)
                    .doOnError(e -> log.error("WebSocket error: {}", e.getMessage()))
                    .then();

            // Periodic PING to keep connection alive
            Flux<Void> pings = Flux.interval(Duration.ofSeconds(20))
                    .flatMap(i -> session.send(Flux.just(session.textMessage("{\"op\":\"ping\"}"))))
                    .takeUntilOther(receiveMessages);

            return Mono.when(sendSub, receiveMessages, pings);
        })
                .retryWhen(Retry.backoff(Integer.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal -> log.warn("WebSocket connection lost, retrying... (Attempt {})",
                                retrySignal.totalRetries() + 1)))
                .subscribe(
                        null,
                        error -> log.error("Fatal WebSocket connection error: {}", error.getMessage()));
    }

    private void processWsMessage(String message) {
        log.debug("Received WebSocket message: {}", message);
        try {
            if (message.contains("\"topic\":\"ticker.")) {
                BybitWebSocketTickerResponse response = objectMapper.readValue(message,
                        BybitWebSocketTickerResponse.class);
                if (response.getData() != null) {
                    TickerUpdate update = mapToDomain(response);
                    tickerCache.put(update.getSymbol(), update);
                    tickerSink.tryEmitNext(update);
                }
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }

    private TickerUpdate mapToDomain(BybitWebSocketTickerResponse response) {
        var data = response.getData();
        return TickerUpdate.builder()
                .symbol(data.getSymbol())
                .lastPrice(new BigDecimal(data.getLastPrice()))
                .highPrice24h(new BigDecimal(data.getHighPrice24h()))
                .lowPrice24h(new BigDecimal(data.getLowPrice24h()))
                .volume24h(new BigDecimal(data.getVolume24h()))
                .timestamp(Instant.ofEpochMilli(response.getTimestamp()))
                .build();
    }

    private String toJsonArray(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    public TickerUpdate getTicker(String symbol) {
        return tickerCache.get(symbol);
    }

    public Flux<TickerUpdate> getTickerStream() {
        return tickerSink.asFlux();
    }
}
