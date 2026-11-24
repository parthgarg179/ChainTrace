package com.chaintrace.company;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * How a company stores its customers' passwords. Customers log in with their
 * existing company account, so ChainTrace must check passwords the company's way.
 *
 * Each constant implements {@link #matches} differently (an enum with
 * constant-specific behaviour, i.e. polymorphism without a class hierarchy).
 */
public enum PasswordEncoding {

    /** Industry standard: salted, slow hash. Recommended for real deployments. */
    BCRYPT {
        @Override
        public boolean matches(String rawPassword, String storedHash) {
            return Encoders.BCRYPT.matches(rawPassword, storedHash);
        }
    },

    /** Hex-encoded SHA-256 of the password. Used by the two demo companies. */
    SHA256 {
        @Override
        public boolean matches(String rawPassword, String storedHash) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] actual = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
                byte[] expected = HexFormat.of().parseHex(storedHash.trim().toLowerCase(Locale.ROOT));
                // Constant-time comparison, so response time doesn't leak how many bytes matched.
                return MessageDigest.isEqual(actual, expected);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 is not available on this JVM", e);
            } catch (IllegalArgumentException e) {
                return false; // stored value is not valid hex
            }
        }
    };

    /** @return true if the password the customer typed matches the value stored by the company. */
    public abstract boolean matches(String rawPassword, String storedHash);

    /** Lazy holder: the BCrypt encoder is created once, and only if some company uses BCRYPT. */
    private static final class Encoders {
        private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    }
}
