package com.chaintrace.model;

/**
 * A customer account as read from the company's database at login time.
 * The password hash is used once to check the login and is never stored by ChainTrace.
 */
public record CustomerAccount(String customerId, String fullName, String passwordHash) {
}
