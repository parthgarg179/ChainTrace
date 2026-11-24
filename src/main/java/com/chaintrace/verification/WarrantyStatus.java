package com.chaintrace.verification;

import java.time.LocalDate;

/** Derived from the warranty expiry date the company returns. */
public enum WarrantyStatus {
    ACTIVE,
    EXPIRED,
    UNKNOWN;

    public static WarrantyStatus of(LocalDate expiry, LocalDate today) {
        if (expiry == null) {
            return UNKNOWN;
        }
        return expiry.isBefore(today) ? EXPIRED : ACTIVE;
    }
}
