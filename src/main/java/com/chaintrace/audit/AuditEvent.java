package com.chaintrace.audit;

/** What happened. Stored as text in the audit_log table. */
public enum AuditEvent {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOOKUP_VERIFIED,
    LOOKUP_NOT_FOUND,
    LOOKUP_INVALID_ID
}
