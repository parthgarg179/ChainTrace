package com.chaintrace.company;

import com.chaintrace.web.RequestAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets the widget label itself correctly ("Enter IMEI" vs "Enter VIN")
 * without hard-coding anything per company.
 */
@RestController
public class CompanyController {

    /** Public, safe subset of a company's configuration (no keys, no SQL, no DB details). */
    public record CompanyInfoResponse(String id, String displayName, ProductType productType,
                                      String idLabel, String idHint) {
    }

    @GetMapping("/api/v1/company")
    public CompanyInfoResponse currentCompany(@RequestAttribute(RequestAttributes.COMPANY) CompanyConfig company) {
        return new CompanyInfoResponse(company.id(), company.displayName(), company.productType(),
                company.idLabel(), company.idHint());
    }
}
