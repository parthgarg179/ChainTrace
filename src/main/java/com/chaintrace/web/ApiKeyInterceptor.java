package com.chaintrace.web;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.company.CompanyRegistry;
import com.chaintrace.exception.InvalidApiKeyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Step 1 of every protected API call: which licensed company is calling?
 * Reads the X-ChainTrace-Key header and stores the matching company on the request.
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final CompanyRegistry companies;

    public ApiKeyInterceptor(CompanyRegistry companies) {
        this.companies = companies;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // CORS pre-flight carries no headers
        }
        String apiKey = request.getHeader(RequestAttributes.API_KEY_HEADER);
        CompanyConfig company = companies.findByApiKey(apiKey).orElseThrow(InvalidApiKeyException::new);
        request.setAttribute(RequestAttributes.COMPANY, company);
        return true;
    }
}
