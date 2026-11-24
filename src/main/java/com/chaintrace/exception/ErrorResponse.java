package com.chaintrace.exception;

import java.time.Instant;

/** JSON body of every error response. */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
