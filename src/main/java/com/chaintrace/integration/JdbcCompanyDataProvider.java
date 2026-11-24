package com.chaintrace.integration;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.exception.CompanyDataUnavailableException;
import com.chaintrace.model.CustomerAccount;
import com.chaintrace.model.OwnershipRecord;
import com.chaintrace.model.ProductRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.chaintrace.integration.StandardColumns.*;

/**
 * Generic, configuration-driven implementation of {@link CompanyDataProvider}.
 *
 * It runs the SQL from the company's YAML file against the company's database,
 * using named parameters (:id, :email), never string concatenation, so customer
 * input can't change the query (no SQL injection). The same code serves every company.
 */
@Component
public class JdbcCompanyDataProvider implements CompanyDataProvider {

    private final CompanyDataSourceRegistry dataSources;

    public JdbcCompanyDataProvider(CompanyDataSourceRegistry dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public Optional<CustomerAccount> findCustomerByEmail(CompanyConfig company, String email) {
        List<Map<String, Object>> rows = query(company, company.queries().login(),
                new MapSqlParameterSource("email", email));
        return rows.stream().findFirst().map(row -> {
            RowValues.requireColumns(row, REQUIRED_LOGIN, "login", company.id());
            return new CustomerAccount(
                    RowValues.asString(row.get(CUSTOMER_ID)),
                    RowValues.asString(row.get(FULL_NAME)),
                    RowValues.asString(row.get(PASSWORD_HASH)));
        });
    }

    @Override
    public Optional<ProductRecord> findProduct(CompanyConfig company, String productId) {
        List<Map<String, Object>> rows = query(company, company.queries().product(),
                new MapSqlParameterSource("id", productId));
        return rows.stream().findFirst().map(row -> {
            RowValues.requireColumns(row, REQUIRED_PRODUCT, "product", company.id());
            // remove(...) takes the standard columns out; whatever remains becomes "details".
            return new ProductRecord(
                    RowValues.asString(row.remove(MODEL)),
                    RowValues.asLocalDate(row.remove(MANUFACTURE_DATE)),
                    RowValues.asLocalDate(row.remove(WARRANTY_EXPIRY)),
                    RowValues.asString(row.remove(SOLD_IN)),
                    RowValues.asString(row.remove(ALERT)),
                    row);
        });
    }

    @Override
    public List<OwnershipRecord> findOwnershipHistory(CompanyConfig company, String productId) {
        List<Map<String, Object>> rows = query(company, company.queries().ownership(),
                new MapSqlParameterSource("id", productId));
        return rows.stream()
                .map(row -> {
                    RowValues.requireColumns(row, REQUIRED_OWNERSHIP, "ownership", company.id());
                    return new OwnershipRecord(
                            RowValues.asString(row.remove(OWNER_NAME)),
                            RowValues.asLocalDate(row.remove(PURCHASE_DATE)),
                            RowValues.asString(row.remove(CHANNEL)),
                            RowValues.asString(row.remove(LOCATION)),
                            row);
                })
                .toList();
    }

    /** Runs one company query; any database failure becomes a friendly 503 error. */
    private List<Map<String, Object>> query(CompanyConfig company, String sql, MapSqlParameterSource params) {
        return guarded(company, () -> dataSources.jdbcFor(company)
                .query(sql, params, (rs, rowNum) -> RowValues.readRow(rs)));
    }

    private static <T> T guarded(CompanyConfig company, Supplier<T> databaseCall) {
        try {
            return databaseCall.get();
        } catch (DataAccessException e) {
            throw new CompanyDataUnavailableException(company.displayName(), e);
        }
    }
}
