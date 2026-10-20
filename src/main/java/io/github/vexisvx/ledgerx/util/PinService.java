package io.github.vexisvx.ledgerx.util;

import io.github.vexisvx.ledgerx.config.AppConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PinService {

    /** Set a PIN. Stored as "salt:hash" in preferences. */
    public static void setPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}"))
            throw new IllegalArgumentException("PIN must be 4 digits");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String hash = hash(pin, salt);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        AppConfig.setPin(saltB64 + ":" + hash);
    }

    public static boolean isEnabled() {
        String stored = AppConfig.getPin();
        return stored != null && !stored.isEmpty();
    }

    public static boolean verify(String pin) {
        String stored = AppConfig.getPin();
        if (stored == null || stored.isEmpty()) return true;
        String[] parts = stored.split(":", 2);
        if (parts.length != 2) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expected = parts[1];
            String actual = hash(pin, salt);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                          actual.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    public static void clearPin() {
        AppConfig.setPin("");
    }

    private static String hash(String pin, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
