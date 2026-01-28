package com.cryptobot.adapter.bybit;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Utility for generating Bybit API v5 request signatures
 */
@Component
public class BybitSignatureGenerator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates a signature for Bybit API v5
     * 
     * @param apiSecret  The user's API secret
     * @param timestamp  The current timestamp in milliseconds
     * @param apiKey     The user's API key
     * @param recvWindow The receive window in milliseconds
     * @param payload    The request body (for POST) or query string (for GET)
     * @return Hex-encoded signature
     */
    public String generate(String apiSecret, String timestamp, String apiKey, String recvWindow, String payload) {
        try {
            String message = timestamp + apiKey + recvWindow + payload;

            Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Bybit signature", e);
        }
    }
}
