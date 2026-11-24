package com.chaintrace.integration;

import java.util.Set;

/**
 * ChainTrace's query contract: the column aliases a company's SQL must return.
 * Any column not listed here is passed through as an extra "detail".
 */
public final class StandardColumns {

    // login query
    public static final String CUSTOMER_ID = "customer_id";
    public static final String FULL_NAME = "full_name";
    public static final String PASSWORD_HASH = "password_hash";

    // product query
    public static final String MODEL = "model";
    public static final String MANUFACTURE_DATE = "manufacture_date";
    public static final String WARRANTY_EXPIRY = "warranty_expiry";
    public static final String SOLD_IN = "sold_in";
    public static final String ALERT = "alert";

    // ownership query
    public static final String OWNER_NAME = "owner_name";
    public static final String PURCHASE_DATE = "purchase_date";
    public static final String CHANNEL = "channel";
    public static final String LOCATION = "location";

    /** Columns each query must return, used to give a clear error when a YAML query is wrong. */
    public static final Set<String> REQUIRED_LOGIN = Set.of(CUSTOMER_ID, FULL_NAME, PASSWORD_HASH);
    public static final Set<String> REQUIRED_PRODUCT = Set.of(MODEL);
    public static final Set<String> REQUIRED_OWNERSHIP = Set.of(OWNER_NAME, PURCHASE_DATE);

    private StandardColumns() {
        // constants only
    }
}
