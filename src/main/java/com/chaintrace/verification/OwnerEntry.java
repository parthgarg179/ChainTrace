package com.chaintrace.verification;

import java.time.LocalDate;
import java.util.Map;

/**
 * One owner in the ownership chain shown to the customer.
 *
 * @param sequence      1 = first owner
 * @param ownerName     owner's full name
 * @param purchaseDate  date they acquired the product
 * @param ownedUntil    date the next owner acquired it; null for the current owner
 * @param ownershipDays how long they owned (or have owned) it
 * @param current       true for the present owner
 * @param channel       how it was acquired ("Brand store", "Dealer sale", "Resale")
 * @param location      city/state
 * @param details       extra company fields, e.g. odometer_km
 */
public record OwnerEntry(
        int sequence,
        String ownerName,
        LocalDate purchaseDate,
        LocalDate ownedUntil,
        Long ownershipDays,
        boolean current,
        String channel,
        String location,
        Map<String, Object> details) {
}
