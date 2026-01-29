package com.cryptobot.service;

import com.cryptobot.adapter.bybit.dto.BybitWebSocketTickerResponse;
import com.cryptobot.domain.model.TickerUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    private MarketDataService marketDataService;

    @Mock
    private WebSocketClient webSocketClient;

    @Mock
    private WebSocketSession webSocketSession;

    @Mock
    private WebClient.Builder webClientBuilder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        marketDataService = new MarketDataService(objectMapper, webSocketClient, webClientBuilder);
        ReflectionTestUtils.setField(marketDataService, "wsUrl", "ws://localhost:8080");
        ReflectionTestUtils.setField(marketDataService, "topics", List.of("ticker.BTCUSDT"));
    }

    @Test
    void testMessageProcessing() throws Exception {
        // Arrange
        String message = "{" +
                "\"topic\":\"ticker.BTCUSDT\"," +
                "\"type\":\"delta\"," +
                "\"ts\":1672531200000," +
                "\"data\":{" +
                "\"symbol\":\"BTCUSDT\"," +
                "\"lastPrice\":\"45000.50\"," +
                "\"highPrice24h\":\"46000.00\"," +
                "\"lowPrice24h\":\"44000.00\"," +
                "\"prevPrice24h\":\"44500.00\"," +
                "\"volume24h\":\"100.5\"," +
                "\"turnover24h\":\"4500000\"" +
                "}" +
                "}";

        // Act - Invoke private method via reflection or make it package-private
        ReflectionTestUtils.invokeMethod(marketDataService, "processMessage", message);

        // Assert
        TickerUpdate ticker = marketDataService.getTicker("BTCUSDT");
        assertNotNull(ticker);
        assertEquals(new BigDecimal("45000.50"), ticker.getLastPrice());
        assertEquals("BTCUSDT", ticker.getSymbol());
    }

    @Test
    void testTickerStreamEmitsUpdate() {
        // Arrange
        String message = "{\"topic\":\"ticker.BTCUSDT\",\"ts\":1672531200000,\"data\":{\"symbol\":\"BTCUSDT\",\"lastPrice\":\"45000.50\",\"highPrice24h\":\"46000\",\"lowPrice24h\":\"44000\",\"volume24h\":\"100\"}}";

        // Act & Assert
        Flux<TickerUpdate> stream = marketDataService.getTickerStream();

        // Use a background thread to process the message so the stream can catch it
        StepVerifier.create(stream)
                .then(() -> ReflectionTestUtils.invokeMethod(marketDataService, "processMessage", message))
                .expectNextMatches(update -> update.getSymbol().equals("BTCUSDT")
                        && update.getLastPrice().equals(new BigDecimal("45000.50")))
                .thenCancel()
                .verify();
    }
}
