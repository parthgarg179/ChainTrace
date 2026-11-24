package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/** The ID is well-formed but the company has no record of it. */
public class ProductNotFoundException extends ChainTraceException {

    public ProductNotFoundException(String idLabel, String productId, String companyName) {
        super("No " + companyName + " product is registered with " + idLabel + " " + productId + ".");
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String errorCode() {
        return "PRODUCT_NOT_FOUND";
    }
}
