package com.chaintrace.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data generates the SQL from the method names. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop50ByCompanyIdOrderByOccurredAtDesc(String companyId);

    long countByCompanyIdAndEvent(String companyId, AuditEvent event);
}
