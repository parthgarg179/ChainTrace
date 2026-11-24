package com.chaintrace.verification;

import com.chaintrace.company.ProductType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * What the customer sees after a successful lookup.
 * Built fresh from the company's database on every request; never stored.
 */
public record ProductHistoryResponse(
        String status,
        String companyName,
        ProductType productType,
        String idLabel,
        String productId,
        Instant verifiedAt,
        Product product,
        String currentOwner,
        int previousOwnerCount,
        List<OwnerEntry> owners) {

    /** Product facts plus derived warranty status. */
    public record Product(
            String model,
            LocalDate manufactureDate,
            LocalDate warrantyExpiry,
            WarrantyStatus warrantyStatus,
            String soldIn,
            String alert,
            Map<String, Object> details) {
    }

    public static final String STATUS_VERIFIED = "VERIFIED";
}
