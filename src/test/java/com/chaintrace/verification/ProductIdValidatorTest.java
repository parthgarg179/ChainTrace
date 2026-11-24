package com.chaintrace.verification;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.company.PasswordEncoding;
import com.chaintrace.company.ProductType;
import com.chaintrace.exception.InvalidProductIdException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductIdValidatorTest {

    private final ProductIdValidator validator = new ProductIdValidator();

    private static CompanyConfig company(String pattern, boolean luhn, String label) {
        return new CompanyConfig("test", "Test Co", ProductType.OTHER, label, null, pattern, luhn,
                "key", PasswordEncoding.SHA256, null, null);
    }

    @Test
    void luhnAcceptsValidImei() {
        assertTrue(ProductIdValidator.passesLuhn("356098123456786"));
    }

    @Test
    void luhnRejectsWrongCheckDigit() {
        assertFalse(ProductIdValidator.passesLuhn("356098123456789"));
    }

    @Test
    void normalizeStripsSpacesAndDashesAndUppercases() {
        assertEquals("WBA5R1C0XK8B45210", validator.normalize(" wba-5r1c0 xk8b45210 "));
    }

    @Test
    void validImeiPasses() {
        CompanyConfig phone = company("^[0-9]{15}$", true, "IMEI");
        assertEquals("356098123456786", validator.normalizeAndValidate(phone, "3560 9812 3456 786"));
    }

    @Test
    void imeiWithBadCheckDigitFails() {
        CompanyConfig phone = company("^[0-9]{15}$", true, "IMEI");
        assertThrows(InvalidProductIdException.class, () -> validator.normalizeAndValidate(phone, "356098123456789"));
    }

    @Test
    void vinWithLetterOFails() {
        CompanyConfig bmw = company("^[A-HJ-NPR-Z0-9]{17}$", false, "VIN");
        assertThrows(InvalidProductIdException.class, () -> validator.normalizeAndValidate(bmw, "WBA5R1COXK8B45210"));
    }

    @Test
    void emptyIdFails() {
        CompanyConfig bmw = company("^[A-HJ-NPR-Z0-9]{17}$", false, "VIN");
        assertThrows(InvalidProductIdException.class, () -> validator.normalizeAndValidate(bmw, "   "));
    }
}
