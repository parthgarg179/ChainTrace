package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/** The ID typed by the customer is not a valid IMEI / VIN / etc. for this company. */
public class InvalidProductIdException extends ChainTraceException {

    public InvalidProductIdException(String message) {
        super(message);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String errorCode() {
        return "INVALID_PRODUCT_ID";
    }
}
