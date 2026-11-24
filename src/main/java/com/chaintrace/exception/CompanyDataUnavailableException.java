package com.chaintrace.exception;

import org.springframework.http.HttpStatus;

/**
 * The company's database could not be read (down, unreachable, or a broken query
 * in its YAML file). ChainTrace holds no copy of the data, so it cannot fall back.
 */
public class CompanyDataUnavailableException extends ChainTraceException {

    public CompanyDataUnavailableException(String companyName, Throwable cause) {
        super(companyName + "'s records are temporarily unavailable. Please try again shortly.", cause);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    @Override
    public String errorCode() {
        return "COMPANY_DATA_UNAVAILABLE";
    }
}
