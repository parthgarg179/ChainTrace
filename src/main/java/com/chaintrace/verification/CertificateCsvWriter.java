package com.chaintrace.verification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

/**
 * Writes an ownership certificate as CSV (opens in Excel), in memory,
 * using Java I/O streams and writers. No temporary files are created.
 */
public final class CertificateCsvWriter {

    private CertificateCsvWriter() {
    }

    public static byte[] write(ProductHistoryResponse history) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // try-with-resources closes (and flushes) the writer automatically
        try (Writer out = new OutputStreamWriter(bytes, StandardCharsets.UTF_8)) {
            out.write('﻿'); // byte-order mark so Excel reads UTF-8 names correctly

            line(out, "ChainTrace Ownership Certificate");
            line(out, "Company", history.companyName());
            line(out, history.idLabel(), history.productId());
            line(out, "Model", history.product().model());
            line(out, "Manufactured", text(history.product().manufactureDate()));
            LocalDate expiry = history.product().warrantyExpiry();
            line(out, "Warranty", history.product().warrantyStatus() + (expiry == null ? "" : " (until " + expiry + ")"));
            line(out, "Alert", history.product().alert() == null ? "None" : history.product().alert());
            line(out, "Current owner", history.currentOwner());
            line(out, "Verified at (UTC)", history.verifiedAt().toString());
            line(out);
            line(out, "#", "Owner", "Purchased on", "Owned until", "Days owned", "Channel", "Location", "Other details");

            for (OwnerEntry owner : history.owners()) {
                line(out,
                        String.valueOf(owner.sequence()),
                        owner.ownerName(),
                        text(owner.purchaseDate()),
                        owner.current() ? "Present" : text(owner.ownedUntil()),
                        owner.ownershipDays() == null ? "" : owner.ownershipDays().toString(),
                        owner.channel(),
                        owner.location(),
                        describe(owner.details()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not build certificate", e);
        }
        return bytes.toByteArray();
    }

    private static void line(Writer out, String... cells) throws IOException {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            out.write(escape(cells[i]));
        }
        out.write("\r\n");
    }

    /** CSV rule: wrap in quotes if the value has a comma, quote or newline; double any quotes. */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private static String text(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private static String describe(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        details.forEach((key, value) -> {
            if (value != null) {
                if (!sb.isEmpty()) {
                    sb.append("; ");
                }
                sb.append(key).append('=').append(value);
            }
        });
        return sb.toString();
    }
}
