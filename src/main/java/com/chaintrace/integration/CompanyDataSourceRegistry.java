package com.chaintrace.integration;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.company.CompanyRegistry;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps one small, read-only connection pool per licensed company.
 *
 * Pools are lazy: no connection is opened until the first lookup, so ChainTrace
 * still starts even if one company's database is down.
 *
 * These are deliberately NOT Spring beans of type DataSource, so Spring Boot's
 * auto-configuration keeps using spring.datasource for ChainTrace's own database.
 */
@Component
public class CompanyDataSourceRegistry implements DisposableBean {

    private static final int MAX_CONNECTIONS_PER_COMPANY = 5;

    private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final Map<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();

    public CompanyDataSourceRegistry(CompanyRegistry companyRegistry) {
        for (CompanyConfig company : companyRegistry.all()) {
            HikariDataSource pool = new HikariDataSource();
            pool.setPoolName("company-" + company.id());
            pool.setJdbcUrl(company.datasource().url());
            pool.setUsername(company.datasource().username());
            pool.setPassword(company.datasource().password());
            pool.setReadOnly(true);                 // ChainTrace never writes to a company database
            pool.setMaximumPoolSize(MAX_CONNECTIONS_PER_COMPANY);
            pool.setMinimumIdle(0);                 // lazy: open connections only when needed
            pool.setInitializationFailTimeout(-1);  // don't fail startup if the company DB is down

            pools.put(company.id(), pool);
            templates.put(company.id(), new NamedParameterJdbcTemplate(pool));
        }
    }

    /** JDBC helper bound to the given company's database. */
    public NamedParameterJdbcTemplate jdbcFor(CompanyConfig company) {
        NamedParameterJdbcTemplate template = templates.get(company.id());
        if (template == null) {
            throw new IllegalStateException("No connection pool for company " + company.id());
        }
        return template;
    }

    @Override
    public void destroy() {
        pools.values().forEach(HikariDataSource::close);
    }
}
