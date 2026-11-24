package com.chaintrace.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * One row of a product's ownership history, as read from the company's database.
 *
 * @param ownerName    full name of the owner
 * @param purchaseDate date this owner acquired the product
 * @param channel      how it was acquired, e.g. "Brand store", "Dealer sale", "Resale"
 * @param location     city / state of the owner or store (may be null)
 * @param details      extra columns, e.g. odometer reading at transfer
 */
public record OwnershipRecord(
        String ownerName,
        LocalDate purchaseDate,
        String channel,
        String location,
        Map<String, Object> details) {
}
