package com.chaintrace.company;

import java.util.regex.Pattern;

/**
 * One licensed company, loaded from a file in resources/companies/*.yml.
 * Everything company-specific lives here; the rest of ChainTrace is generic.
 *
 * @param id               short unique id used in URLs, e.g. "bmw-india"
 * @param displayName      name shown to customers, e.g. "BMW India"
 * @param productType      kind of product the company sells
 * @param idLabel          what the product's unique ID is called, e.g. "IMEI" or "VIN"
 * @param idHint           help text shown under the search box
 * @param idPattern        regular expression a valid product ID must match
 * @param luhnCheck        whether the ID also carries a Luhn check digit (true for IMEI)
 * @param apiKey           licence key the company's website sends with every request
 * @param passwordEncoding how the company stores customer passwords
 * @param datasource       connection to the company's own (read-only) database
 * @param queries          the three SELECTs that translate the company's tables into ChainTrace's format
 */
public record CompanyConfig(
        String id,
        String displayName,
        ProductType productType,
        String idLabel,
        String idHint,
        String idPattern,
        boolean luhnCheck,
        String apiKey,
        PasswordEncoding passwordEncoding,
        DataSourceSettings datasource,
        Queries queries) {

    /** JDBC details of the company's database. */
    public record DataSourceSettings(String url, String username, String password) {
    }

    /** SQL that maps the company's schema onto ChainTrace's standard columns. */
    public record Queries(String login, String product, String ownership) {
    }

    /** Compiled once per call site is cheap enough; kept here so callers don't repeat the logic. */
    public Pattern compiledIdPattern() {
        return Pattern.compile(idPattern);
    }

    /** Fails fast at startup if a company file is incomplete, instead of failing on the first lookup. */
    public void validate(String sourceName) {
        requireText(id, "id", sourceName);
        requireText(displayName, "displayName", sourceName);
        requireText(idLabel, "idLabel", sourceName);
        requireText(idPattern, "idPattern", sourceName);
        requireText(apiKey, "apiKey", sourceName);
        if (productType == null) {
            throw new IllegalStateException(sourceName + ": 'productType' is required");
        }
        if (passwordEncoding == null) {
            throw new IllegalStateException(sourceName + ": 'passwordEncoding' is required");
        }
        if (datasource == null) {
            throw new IllegalStateException(sourceName + ": 'datasource' section is required");
        }
        requireText(datasource.url(), "datasource.url", sourceName);
        if (queries == null) {
            throw new IllegalStateException(sourceName + ": 'queries' section is required");
        }
        requireText(queries.login(), "queries.login", sourceName);
        requireText(queries.product(), "queries.product", sourceName);
        requireText(queries.ownership(), "queries.ownership", sourceName);
        compiledIdPattern(); // throws PatternSyntaxException if the regex is broken
    }

    private static void requireText(String value, String field, String sourceName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(sourceName + ": '" + field + "' is required");
        }
    }
}
