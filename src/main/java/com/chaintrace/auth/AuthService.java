package com.chaintrace.auth;

import com.chaintrace.audit.AuditEvent;
import com.chaintrace.audit.AuditService;
import com.chaintrace.company.CompanyConfig;
import com.chaintrace.exception.InvalidCredentialsException;
import com.chaintrace.integration.CompanyDataProvider;
import com.chaintrace.model.CustomerAccount;
import org.springframework.stereotype.Service;

/**
 * Company-side login: the customer signs in with the account they already have
 * with the company. ChainTrace checks the password against the company's database
 * (using that company's password scheme) and issues a short-lived token.
 */
@Service
public class AuthService {

    private final CompanyDataProvider companyData;
    private final SessionStore sessions;
    private final AuditService audit;

    public AuthService(CompanyDataProvider companyData, SessionStore sessions, AuditService audit) {
        this.companyData = companyData;
        this.sessions = sessions;
        this.audit = audit;
    }

    public CustomerSession login(CompanyConfig company, String email, String password) {
        CustomerAccount account = companyData.findCustomerByEmail(company, email.trim())
                .filter(found -> found.passwordHash() != null
                        && company.passwordEncoding().matches(password, found.passwordHash()))
                .orElse(null);

        if (account == null) {
            audit.record(company.id(), null, AuditEvent.LOGIN_FAILED, null);
            throw new InvalidCredentialsException();
        }

        CustomerSession session = sessions.create(company.id(), account.customerId(), account.fullName());
        audit.record(company.id(), account.customerId(), AuditEvent.LOGIN_SUCCESS, null);
        return session;
    }

    public void logout(String token) {
        sessions.remove(token);
    }
}
