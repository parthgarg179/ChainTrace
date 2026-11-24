package com.chaintrace.demo;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.company.CompanyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * DEMO ONLY. Creates and fills the two mock company databases from
 * resources/mock-companies/{companyId}/schema.sql and data.sql.
 *
 * In a real deployment this class is switched off
 * (chaintrace.demo.seed-mock-companies=false) because the company's database
 * already exists. It is kept apart from the rest of the code to make clear that
 * ChainTrace itself never writes to a company database.
 */
@Component
@ConditionalOnProperty(name = "chaintrace.demo.seed-mock-companies", havingValue = "true")
public class MockCompanySeeder implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MockCompanySeeder.class);

    private final CompanyRegistry companies;

    public MockCompanySeeder(CompanyRegistry companies) {
        this.companies = companies;
    }

    /** Runs once at startup, before the web server accepts requests. */
    @Override
    public void afterPropertiesSet() {
        for (CompanyConfig company : companies.all()) {
            ClassPathResource schema = new ClassPathResource("mock-companies/" + company.id() + "/schema.sql");
            ClassPathResource data = new ClassPathResource("mock-companies/" + company.id() + "/data.sql");
            if (!schema.exists()) {
                log.info("No mock data for {}, skipping (expected for real companies)", company.id());
                continue;
            }

            // A separate, writable connection used only for seeding;
            // the lookup path uses the read-only pools in CompanyDataSourceRegistry.
            DriverManagerDataSource seedConnection = new DriverManagerDataSource(
                    company.datasource().url(), company.datasource().username(), company.datasource().password());

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(schema);
            if (data.exists()) {
                populator.addScript(data);
            }
            populator.execute(seedConnection);
            log.info("Mock database for {} created at {}", company.displayName(), company.datasource().url());
        }
    }
}
