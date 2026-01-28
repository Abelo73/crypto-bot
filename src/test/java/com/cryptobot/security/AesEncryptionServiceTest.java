package com.cryptobot.security;

import com.cryptobot.security.impl.AesEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptionServiceTest {

    private AesEncryptionService encryptionService;
    private final String masterKey = Base64.getEncoder().encodeToString(new byte[32]); // 256-bit zero key

    @BeforeEach
    void setUp() {
        encryptionService = new AesEncryptionService();
        ReflectionTestUtils.setField(encryptionService, "masterKeyBase64", masterKey);
        encryptionService.init();
    }

    @Test
    void testEncryptDecrypt() {
        String originalText = "my-secret-api-key-123";

        String encrypted = encryptionService.encrypt(originalText);
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(originalText, decrypted);
    }

    @Test
    void testDifferentEncryptionForSameText() {
        String text = "same-text";

        String encrypted1 = encryptionService.encrypt(text);
        String encrypted2 = encryptionService.encrypt(text);

        // Due to random IV, they should be different
        assertNotEquals(encrypted1, encrypted2);

        assertEquals(text, encryptionService.decrypt(encrypted1));
        assertEquals(text, encryptionService.decrypt(encrypted2));
    }

    @Test
    void testNullHandling() {
        assertNull(encryptionService.encrypt(null));
        assertNull(encryptionService.decrypt(null));
    }
}
