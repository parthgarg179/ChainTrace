package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class of every expected, user-facing error in ChainTrace.
 * Each subclass decides its own HTTP status and machine-readable error code;
 * {@link GlobalExceptionHandler} turns any of them into a clean JSON response.
 */
public abstract class ChainTraceException extends RuntimeException {

    protected ChainTraceException(String message) {
        super(message);
    }

    protected ChainTraceException(String message, Throwable cause) {
        super(message, cause);
    }

    /** HTTP status sent to the client. */
    public abstract HttpStatus status();

    /** Stable code the widget can branch on, e.g. "PRODUCT_NOT_FOUND". */
    public abstract String errorCode();
}
