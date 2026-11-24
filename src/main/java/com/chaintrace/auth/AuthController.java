package com.chaintrace.auth;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.web.RequestAttributes;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login / logout. The company is identified by the licence key header
 * (resolved by ApiKeyInterceptor), so the same endpoint serves every company.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestAttribute(RequestAttributes.COMPANY) CompanyConfig company,
                               @Valid @RequestBody LoginRequest request) {
        CustomerSession session = authService.login(company, request.email(), request.password());
        return new LoginResponse(session.token(), session.customerName(), company.displayName(), session.expiresAt());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = RequestAttributes.AUTHORIZATION_HEADER, required = false) String authorization) {
        authService.logout(RequestAttributes.bearerToken(authorization));
        return ResponseEntity.noContent().build();
    }
}
