package com.chaintrace.auth;

import java.time.Instant;

/**
 * A signed-in customer. Created after a successful login against the company's
 * database and identified by an opaque random token.
 */
public record CustomerSession(
        String token,
        String companyId,
        String customerId,
        String customerName,
        Instant expiresAt) {

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
