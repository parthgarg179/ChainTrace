package com.chaintrace.integration;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.model.CustomerAccount;
import com.chaintrace.model.OwnershipRecord;
import com.chaintrace.model.ProductRecord;

import java.util.List;
import java.util.Optional;

/**
 * The single seam between ChainTrace and a company's data. Everything above this
 * interface (services, controllers, widget) is the same for every company.
 *
 * Implementations:
 *   - {@link JdbcCompanyDataProvider}: reads the company's database directly with the
 *     SQL from its YAML file (used in this prototype).
 *   - Future: an HTTP implementation that calls an API the company exposes instead.
 *
 * All methods are read-only. Nothing returned here is persisted by ChainTrace.
 */
public interface CompanyDataProvider {

    /** Looks up a customer account for login. Empty if the email is unknown. */
    Optional<CustomerAccount> findCustomerByEmail(CompanyConfig company, String email);

    /** Looks up a product by its unique ID. Empty if the company has no such product. */
    Optional<ProductRecord> findProduct(CompanyConfig company, String productId);

    /** Returns every ownership record of the product (any order; the service sorts it). */
    List<OwnershipRecord> findOwnershipHistory(CompanyConfig company, String productId);
}
