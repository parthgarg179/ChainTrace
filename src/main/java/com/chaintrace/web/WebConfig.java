package com.chaintrace.web;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.company.CompanyRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Wires the two security checks onto the API:
 *
 *   /api/v1/company, /api/v1/auth/**  -> licence key only
 *   /api/v1/products/**  -> licence key + customer login
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;
    private final SessionInterceptor sessionInterceptor;
    private final CompanyRegistry companies;

    public WebConfig(ApiKeyInterceptor apiKeyInterceptor, SessionInterceptor sessionInterceptor,
                     CompanyRegistry companies) {
        this.apiKeyInterceptor = apiKeyInterceptor;
        this.sessionInterceptor = sessionInterceptor;
        this.companies = companies;
    }

    /**
     * Spring Boot only serves index.html automatically for "/", so map each demo
     * company folder (/demo/bmw-india/) to its index.html.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (CompanyConfig company : companies.all()) {
            String folder = "/demo/" + company.id();
            registry.addViewController(folder).setViewName("redirect:" + folder + "/");
            registry.addViewController(folder + "/").setViewName("forward:" + folder + "/index.html");
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/v1/**")
                .order(1);

        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/v1/products/**")
                .order(2);
    }
}
