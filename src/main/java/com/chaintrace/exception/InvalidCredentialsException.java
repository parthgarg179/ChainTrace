package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/**
 * Wrong email or password. The message is identical for both cases on purpose,
 * so nobody can find out which emails have accounts.
 */
public class InvalidCredentialsException extends ChainTraceException {

    public InvalidCredentialsException() {
        super("Incorrect email or password.");
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String errorCode() {
        return "INVALID_CREDENTIALS";
    }
}
