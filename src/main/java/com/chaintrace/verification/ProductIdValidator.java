package com.chaintrace.verification;

import com.chaintrace.company.CompanyConfig;
import com.chaintrace.exception.InvalidProductIdException;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Cleans up and checks a product ID before any database is touched.
 * Catches typos early ("that isn't a valid IMEI") instead of returning "not found".
 */
@Component
public class ProductIdValidator {

    /** Removes spaces and dashes people often type, and upper-cases (VINs are upper-case). */
    public String normalize(String rawId) {
        if (rawId == null) {
            return "";
        }
        return rawId.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    /** @return the normalized ID if valid for this company; throws otherwise. */
    public String normalizeAndValidate(CompanyConfig company, String rawId) {
        String id = normalize(rawId);
        if (id.isEmpty()) {
            throw new InvalidProductIdException("Please enter a " + company.idLabel() + ".");
        }
        if (!company.compiledIdPattern().matcher(id).matches()) {
            throw new InvalidProductIdException("'" + id + "' is not a valid " + company.idLabel()
                    + ". " + (company.idHint() == null ? "" : company.idHint()));
        }
        if (company.luhnCheck() && !passesLuhn(id)) {
            throw new InvalidProductIdException("'" + id + "' is not a valid " + company.idLabel()
                    + " (check digit does not match - please re-check the number).");
        }
        return id;
    }

    /**
     * Luhn checksum, the check digit used by IMEIs (and credit cards).
     * From the right, double every second digit; subtract 9 if the result exceeds 9;
     * the total must be a multiple of 10.
     */
    public static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            char c = digits.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int digit = c - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return !digits.isEmpty() && sum % 10 == 0;
    }
}
