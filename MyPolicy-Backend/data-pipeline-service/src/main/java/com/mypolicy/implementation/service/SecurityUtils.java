package com.mypolicy.implementation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * PII encryption at rest. Uses AES via Spring Security TextEncryptor.
 * Encrypts PAN, Mobile, Email before saving to MongoDB.
 */
@Component
public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private final TextEncryptor encryptor;

    public SecurityUtils(@Value("${mypolicy.encryption.secret:MyPolicy2DefaultSecretKey32Chars!!}") String secret) {
        // Hex-encoded salt, at least 8 bytes (16 hex chars)
        String salt = "6d79506f6c69637932";
        this.encryptor = Encryptors.text(secret, salt);
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        try {
            return encryptor.encrypt(plainText);
        } catch (Exception e) {
            log.warn("Encryption failed for value: {}", e.getMessage());
            return null;
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) return null;
        try {
            return encryptor.decrypt(cipherText);
        } catch (Exception e) {
            log.warn("Decryption failed: {}", e.getMessage());
            return null;
        }
    }
}
