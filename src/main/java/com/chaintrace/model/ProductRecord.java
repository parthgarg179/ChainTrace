package com.chaintrace.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * A product as read from the company's database, in ChainTrace's standard shape.
 *
 * @param model           product model, e.g. "BMW X1"
 * @param manufactureDate date of manufacture (may be null)
 * @param warrantyExpiry  last day of warranty (may be null)
 * @param soldIn          country or state of first sale / registration (may be null)
 * @param alert           company-raised warning such as "Reported stolen" (null if none)
 * @param details         any extra columns the company chose to expose, in query order
 */
public record ProductRecord(
        String model,
        LocalDate manufactureDate,
        LocalDate warrantyExpiry,
        String soldIn,
        String alert,
        Map<String, Object> details) {
}
