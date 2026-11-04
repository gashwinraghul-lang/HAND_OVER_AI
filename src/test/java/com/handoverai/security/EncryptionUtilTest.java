package com.handoverai.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private final EncryptionUtil encryptionUtil =
            new EncryptionUtil("unit-test-secret-key-32-bytes!!");

    @Test
    void encryptionRoundTripRecoversOriginalValue() {
        String plainText = "ghp_superSecretApiKey_1234567890";

        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertNotEquals(plainText, encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void samePlainTextProducesDifferentCipherText() {
        String first = encryptionUtil.encrypt("connection-key");
        String second = encryptionUtil.encrypt("connection-key");

        assertNotEquals(first, second, "A random IV must be used for every encryption");
        assertEquals("connection-key", encryptionUtil.decrypt(first));
        assertEquals("connection-key", encryptionUtil.decrypt(second));
    }

    @Test
    void masksApiKeysToLastFourCharacters() {
        assertEquals("...7890", EncryptionUtil.maskApiKey("ghp_demo1234567890"));
        assertEquals("...1234", EncryptionUtil.maskApiKey("trello_demo_key1234"));
        assertEquals("****", EncryptionUtil.maskApiKey("abc"));
        assertEquals("****", EncryptionUtil.maskApiKey(null));
    }
}
