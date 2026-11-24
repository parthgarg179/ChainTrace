package com.chaintrace.web;

import com.chaintrace.auth.CustomerSession;
import com.chaintrace.auth.SessionStore;
import com.chaintrace.company.CompanyConfig;
import com.chaintrace.exception.SessionExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Step 2 for lookups: is a customer of THIS company signed in?
 * Runs after {@link ApiKeyInterceptor}. A token issued by one company is rejected
 * on another company's key, so one company's customers can't read another company's records.
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    private final SessionStore sessions;

    public SessionInterceptor(SessionStore sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        CompanyConfig company = (CompanyConfig) request.getAttribute(RequestAttributes.COMPANY);
        String token = RequestAttributes.bearerToken(request.getHeader(RequestAttributes.AUTHORIZATION_HEADER));

        CustomerSession session = sessions.find(token)
                .filter(found -> company != null && found.companyId().equals(company.id()))
                .orElseThrow(SessionExpiredException::new);

        request.setAttribute(RequestAttributes.SESSION, session);
        return true;
    }
}
