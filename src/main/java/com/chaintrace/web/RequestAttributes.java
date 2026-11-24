package com.chaintrace.web;

/** Names shared between the interceptors and the controllers. */
public final class RequestAttributes {

    /** Header the company's website sends with its licence key. */
    public static final String API_KEY_HEADER = "X-ChainTrace-Key";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Request attribute holding the resolved {@link com.chaintrace.company.CompanyConfig}. */
    public static final String COMPANY = "chaintrace.company";

    /** Request attribute holding the logged-in {@link com.chaintrace.auth.CustomerSession}. */
    public static final String SESSION = "chaintrace.session";

    private static final String BEARER_PREFIX = "Bearer ";

    private RequestAttributes() {
    }

    /** Extracts the token from "Bearer abc123", or returns null. */
    public static String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
