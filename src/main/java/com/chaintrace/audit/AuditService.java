package com.chaintrace.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Writes audit entries. A failure to audit is logged but never breaks the customer's request. */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(String companyId, String customerId, AuditEvent event, String productId) {
        try {
            repository.save(new AuditLog(companyId, customerId, event, productId, Instant.now()));
        } catch (RuntimeException e) {
            log.warn("Could not write audit entry {} for {}", event, companyId, e);
        }
    }
}
