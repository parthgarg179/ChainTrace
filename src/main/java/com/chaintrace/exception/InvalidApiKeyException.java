package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/** The request carried no licence key, or one that belongs to no licensed company. */
public class InvalidApiKeyException extends ChainTraceException {

    public InvalidApiKeyException() {
        super("Missing or invalid ChainTrace licence key.");
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String errorCode() {
        return "INVALID_API_KEY";
    }
}
