package com.cryptobot.service;

import com.cryptobot.adapter.bybit.dto.BybitWebSocketTickerResponse;
import com.cryptobot.domain.model.TickerUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
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

/**
 * Service to manage WebSocket connections for real-time market data.
 * Maintains an in-memory cache of the latest ticker prices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final ObjectMapper objectMapper;
    private final WebSocketClient webSocketClient;

    @Value("${bybit.websocket.base-url:wss://stream-testnet.bybit.com/v5/public/spot}")
    private String baseUrl;

    @Value("${bybit.websocket.topics:ticker.BTCUSDT,ticker.ETHUSDT}")
    private List<String> topics;

    private final Map<String, TickerUpdate> tickerCache = new ConcurrentHashMap<>();

    // Sink to broadcast ticker updates to other internal services
    private final Sinks.Many<TickerUpdate> tickerSink = Sinks.many().multicast().directBestEffort();

    private Disposable connectionDisposable;

    @PostConstruct
    public void init() {
        connect();
    }

    @PreDestroy
    public void cleanup() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }

    /**
     * Connect to the WebSocket and subscribe to topics.
     */
    public void connect() {
        log.info("Connecting to Bybit WebSocket: {}", baseUrl);

        connectionDisposable = webSocketClient.execute(URI.create(baseUrl), session -> {
            log.info("WebSocket session established: {}", session.getId());

            // Sending subscription message
            String subMessage = String.format("{\"op\": \"subscribe\", \"args\": %s}",
                    toJsonArray(topics));

            Mono<Void> sendSub = session.send(Flux.just(session.textMessage(subMessage)));

            Mono<Void> receiveMessages = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(this::processMessage)
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

    private void processMessage(String message) {
        try {
            if (message.contains("\"topic\":\"ticker.")) {
                BybitWebSocketTickerResponse response = objectMapper.readValue(message,
                        BybitWebSocketTickerResponse.class);
                if (response.getData() != null) {
                    TickerUpdate update = mapToDomain(response);
                    tickerCache.put(update.getSymbol(), update);
                    tickerSink.tryEmitNext(update);
                    log.debug("Ticker update: {} -> {}", update.getSymbol(), update.getLastPrice());
                }
            } else if (message.contains("\"op\":\"pong\"")) {
                log.trace("Received PONG");
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
