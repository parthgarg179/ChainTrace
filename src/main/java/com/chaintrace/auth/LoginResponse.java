package com.chaintrace.auth;

import java.time.Instant;

/** Returned after a successful login. The widget sends the token back as "Authorization: Bearer <token>". */
public record LoginResponse(String token, String customerName, String companyName, Instant expiresAt) {
}
