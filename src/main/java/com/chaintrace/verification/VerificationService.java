package com.chaintrace.verification;

import com.chaintrace.audit.AuditEvent;
import com.chaintrace.audit.AuditService;
import com.chaintrace.auth.CustomerSession;
import com.chaintrace.company.CompanyConfig;
import com.chaintrace.exception.InvalidProductIdException;
import com.chaintrace.exception.ProductNotFoundException;
import com.chaintrace.integration.CompanyDataProvider;
import com.chaintrace.model.OwnershipRecord;
import com.chaintrace.model.ProductRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * The core use case: "show me the ownership history of product X".
 *
 *   1. validate the ID format (IMEI / VIN rules from the company's config)
 *   2. read the product from the company's database  -> 404 if unknown
 *   3. read its ownership rows from the company's database
 *   4. build the ordered chain and derived facts (current owner, warranty status)
 *   5. write an audit entry (no ownership data, just who/what/when)
 */
@Service
public class VerificationService {

    private final CompanyDataProvider companyData;
    private final ProductIdValidator idValidator;
    private final AuditService audit;
    private final Clock clock;

    public VerificationService(CompanyDataProvider companyData, ProductIdValidator idValidator, AuditService audit) {
        this.companyData = companyData;
        this.idValidator = idValidator;
        this.audit = audit;
        this.clock = Clock.systemDefaultZone();
    }

    public ProductHistoryResponse getHistory(CompanyConfig company, CustomerSession session, String rawProductId) {
        String productId;
        try {
            productId = idValidator.normalizeAndValidate(company, rawProductId);
        } catch (InvalidProductIdException e) {
            audit.record(company.id(), session.customerId(), AuditEvent.LOOKUP_INVALID_ID, truncate(rawProductId));
            throw e;
        }

        ProductRecord product = companyData.findProduct(company, productId).orElse(null);
        if (product == null) {
            audit.record(company.id(), session.customerId(), AuditEvent.LOOKUP_NOT_FOUND, productId);
            throw new ProductNotFoundException(company.idLabel(), productId, company.displayName());
        }

        LocalDate today = LocalDate.now(clock);
        List<OwnershipRecord> records = companyData.findOwnershipHistory(company, productId);
        List<OwnerEntry> owners = OwnershipChainBuilder.build(records, today);

        String currentOwner = owners.isEmpty() ? null : owners.get(owners.size() - 1).ownerName();
        int previousOwners = Math.max(0, owners.size() - 1);

        ProductHistoryResponse.Product productView = new ProductHistoryResponse.Product(
                product.model(),
                product.manufactureDate(),
                product.warrantyExpiry(),
                WarrantyStatus.of(product.warrantyExpiry(), today),
                product.soldIn(),
                product.alert(),
                product.details());

        audit.record(company.id(), session.customerId(), AuditEvent.LOOKUP_VERIFIED, productId);

        return new ProductHistoryResponse(
                ProductHistoryResponse.STATUS_VERIFIED,
                company.displayName(),
                company.productType(),
                company.idLabel(),
                productId,
                Instant.now(clock),
                productView,
                currentOwner,
                previousOwners,
                owners);
    }

    /** Invalid input can be anything; keep the audit column small. */
    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 40 ? value : value.substring(0, 40);
    }
}
