package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/** No login token, an unknown or expired one, or a token issued for another company. */
public class SessionExpiredException extends ChainTraceException {

    public SessionExpiredException() {
        super("Please sign in to continue.");
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String errorCode() {
        return "SESSION_EXPIRED";
    }
}
