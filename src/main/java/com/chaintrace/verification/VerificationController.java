package com.chaintrace.verification;

import com.chaintrace.auth.CustomerSession;
import com.chaintrace.company.CompanyConfig;
import com.chaintrace.web.RequestAttributes;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lookup endpoints. Both need the licence key AND a logged-in customer
 * (see WebConfig), which is why company and session arrive as request attributes.
 */
@RestController
@RequestMapping("/api/v1/products")
public class VerificationController {

    private static final MediaType CSV = new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8);

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /** Full ownership history of one product, read live from the company's database. */
    @GetMapping("/{productId}/history")
    public ProductHistoryResponse history(@RequestAttribute(RequestAttributes.COMPANY) CompanyConfig company,
                                          @RequestAttribute(RequestAttributes.SESSION) CustomerSession session,
                                          @PathVariable String productId) {
        return verificationService.getHistory(company, session, productId);
    }

    /** Same data as a downloadable CSV certificate. */
    @GetMapping("/{productId}/certificate.csv")
    public ResponseEntity<byte[]> certificate(@RequestAttribute(RequestAttributes.COMPANY) CompanyConfig company,
                                              @RequestAttribute(RequestAttributes.SESSION) CustomerSession session,
                                              @PathVariable String productId) {
        ProductHistoryResponse history = verificationService.getHistory(company, session, productId);
        String filename = "chaintrace-" + history.productId() + ".csv";
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(CertificateCsvWriter.write(history));
    }
}
