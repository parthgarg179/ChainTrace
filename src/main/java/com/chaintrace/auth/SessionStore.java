package com.chaintrace.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of logged-in customers, keyed by token.
 *
 * A ConcurrentHashMap is used because many HTTP requests (threads) read and write
 * sessions at the same time. Expired sessions are removed lazily.
 *
 * Production note: with several ChainTrace servers this would move to a shared
 * store (e.g. Redis) or be replaced by signed JWTs.
 */
@Component
public class SessionStore {

    private static final int TOKEN_BYTES = 32;

    private final Map<String, CustomerSession> sessionsByToken = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration timeToLive;
    private final Clock clock;

    /** Constructor Spring uses (marked because the class has two constructors). */
    @Autowired
    public SessionStore(@Value("${chaintrace.session-ttl-minutes:30}") long ttlMinutes) {
        this(Duration.ofMinutes(ttlMinutes), Clock.systemUTC());
    }

    /** Separate constructor so tests can control time. */
    SessionStore(Duration timeToLive, Clock clock) {
        this.timeToLive = timeToLive;
        this.clock = clock;
    }

    public CustomerSession create(String companyId, String customerId, String customerName) {
        removeExpired();
        String token = newToken();
        CustomerSession session = new CustomerSession(token, companyId, customerId, customerName,
                Instant.now(clock).plus(timeToLive));
        sessionsByToken.put(token, session);
        return session;
    }

    public Optional<CustomerSession> find(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        CustomerSession session = sessionsByToken.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired(Instant.now(clock))) {
            sessionsByToken.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void remove(String token) {
        if (token != null) {
            sessionsByToken.remove(token);
        }
    }

    private void removeExpired() {
        Instant now = Instant.now(clock);
        sessionsByToken.values().removeIf(session -> session.isExpired(now));
    }

    /** 256 random bits, URL-safe Base64: unguessable, unlike a counter or a plain UUID. */
    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
