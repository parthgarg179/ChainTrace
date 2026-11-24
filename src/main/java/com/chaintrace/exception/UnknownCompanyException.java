package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/** A URL referred to a company id that has no file in resources/companies. */
public class UnknownCompanyException extends ChainTraceException {

    public UnknownCompanyException(String companyId) {
        super("No licensed company with id '" + companyId + "'.");
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String errorCode() {
        return "UNKNOWN_COMPANY";
    }
}
