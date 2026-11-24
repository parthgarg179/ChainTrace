package com.chaintrace.company;

import com.chaintrace.exception.UnknownCompanyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads every licensed company from resources/companies/*.yml at startup and
 * answers "which company is this?" by id or by API key.
 *
 * Onboarding a new company = dropping a new YAML file in that folder.
 */
@Component
public class CompanyRegistry {

    private static final Logger log = LoggerFactory.getLogger(CompanyRegistry.class);

    /** Insertion-ordered and read-only after startup, so it is safe to share between threads. */
    private final Map<String, CompanyConfig> companiesById;

    public CompanyRegistry(@Value("${chaintrace.companies-location}") String location) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Resource[] files = new PathMatchingResourcePatternResolver().getResources(location);

        Map<String, CompanyConfig> loaded = new LinkedHashMap<>();
        for (Resource file : files) {
            String sourceName = file.getFilename();
            try (InputStream in = file.getInputStream()) {
                CompanyConfig company = yamlMapper.readValue(in, CompanyConfig.class);
                company.validate(sourceName);
                if (loaded.putIfAbsent(company.id(), company) != null) {
                    throw new IllegalStateException("Duplicate company id '" + company.id() + "' in " + sourceName);
                }
                log.info("Licensed company loaded: {} ({}) from {}", company.displayName(), company.id(), sourceName);
            }
        }
        if (loaded.isEmpty()) {
            throw new IllegalStateException("No company files found at " + location);
        }
        this.companiesById = Collections.unmodifiableMap(loaded);
    }

    public CompanyConfig getById(String companyId) {
        CompanyConfig company = companiesById.get(companyId);
        if (company == null) {
            throw new UnknownCompanyException(companyId);
        }
        return company;
    }

    public Optional<CompanyConfig> findByApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        return companiesById.values().stream()
                .filter(company -> company.apiKey().equals(apiKey))
                .findFirst();
    }

    public Collection<CompanyConfig> all() {
        return companiesById.values();
    }
}
