package com.chaintrace.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One audit entry in ChainTrace's OWN database.
 *
 * This is the only thing ChainTrace stores. It records who looked up what and when
 * (useful for billing the licence and spotting abuse) but holds no owner names,
 * dates or other ownership data. Those stay in the company's database.
 */
@Entity
@Table(name = "audit_log", indexes = @Index(name = "idx_audit_company_time", columnList = "company_id, occurred_at"))
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, length = 60)
    private String companyId;

    /** Company's customer id (not name or email). Null for failed logins. */
    @Column(name = "customer_id", length = 60)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditEvent event;

    @Column(name = "product_id", length = 40)
    private String productId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** Required by JPA. */
    protected AuditLog() {
    }

    public AuditLog(String companyId, String customerId, AuditEvent event, String productId, Instant occurredAt) {
        this.companyId = companyId;
        this.customerId = customerId;
        this.event = event;
        this.productId = productId;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public AuditEvent getEvent() {
        return event;
    }

    public String getProductId() {
        return productId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
