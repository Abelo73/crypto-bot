package com.cryptobot.security;

/**
 * Service for encrypting and decrypting sensitive data like API keys
 */
public interface EncryptionService {

    /**
     * Encrypts the provided plain text
     * 
     * @param plainText The text to encrypt
     * @return The encrypted text, usually base64 encoded along with any metadata
     *         (like IV)
     */
    String encrypt(String plainText);

    /**
     * Decrypts the provided encrypted text
     * 
     * @param encryptedText The text to decrypt (must include IV if required by
     *                      algorithm)
     * @return The original plain text
     */
    String decrypt(String encryptedText);
}
