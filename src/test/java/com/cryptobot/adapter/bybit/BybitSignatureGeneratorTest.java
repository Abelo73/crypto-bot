package com.cryptobot.adapter.bybit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BybitSignatureGeneratorTest {

    private final BybitSignatureGenerator generator = new BybitSignatureGenerator();

    @Test
    void testSignatureGeneration() {
        // Example values (can be adjusted to match official docs if needed)
        String apiSecret = "897fs8d9f7sd9fsd8f9s7d9fsd";
        String timestamp = "1653815024000";
        String apiKey = "Xas8d7f98as7df98as7df";
        String recvWindow = "5000";
        String payload = "{\"symbol\":\"BTCUSDT\",\"side\":\"Buy\",\"orderType\":\"Limit\",\"qty\":\"0.01\",\"price\":\"20000\"}";

        String signature = generator.generate(apiSecret, timestamp, apiKey, recvWindow, payload);

        assertNotNull(signature);
        assertEquals(64, signature.length()); // HMAC-SHA256 hex is 64 chars

        // Verify consistency
        String signature2 = generator.generate(apiSecret, timestamp, apiKey, recvWindow, payload);
        assertEquals(signature, signature2);
    }
}
