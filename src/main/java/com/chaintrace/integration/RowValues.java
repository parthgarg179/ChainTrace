package com.chaintrace.integration;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Small helpers that turn a JDBC row into plain Java values.
 *
 * Different databases return different Java types for the same SQL type
 * (java.sql.Date, Timestamp, LocalDate, String...). These methods hide that,
 * using pattern-matching switches (Java 21).
 */
final class RowValues {

    private RowValues() {
    }

    /**
     * Reads the current row into an ordered map keyed by lower-case column label,
     * so "OWNER_NAME" (H2) and "owner_name" (MySQL/PostgreSQL) behave the same.
     */
    static Map<String, Object> readRow(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            row.put(meta.getColumnLabel(i).toLowerCase(Locale.ROOT), normalize(rs.getObject(i)));
        }
        return row;
    }

    /** Throws a readable error if a company's query does not follow the column contract. */
    static void requireColumns(Map<String, Object> row, Set<String> required, String queryName, String companyId) {
        for (String column : required) {
            if (!row.containsKey(column)) {
                throw new IllegalStateException("Query '" + queryName + "' of company '" + companyId
                        + "' must return a column aliased as '" + column + "'. Got: " + row.keySet());
            }
        }
    }

    static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    static LocalDate asLocalDate(Object value) {
        return switch (value) {
            case null -> null;
            case LocalDate date -> date;
            case LocalDateTime dateTime -> dateTime.toLocalDate();
            case String text when !text.isBlank() -> LocalDate.parse(text.trim().substring(0, 10));
            case String blank -> null;
            default -> throw new IllegalArgumentException("Not a date: " + value + " (" + value.getClass().getName() + ")");
        };
    }

    /** Converts legacy JDBC date/time classes into java.time so JSON output is consistent. */
    private static Object normalize(Object value) {
        return switch (value) {
            case null -> null;
            case java.sql.Date sqlDate -> sqlDate.toLocalDate();
            case Timestamp timestamp -> timestamp.toLocalDateTime();
            case java.util.Date utilDate -> LocalDateTime.ofInstant(utilDate.toInstant(), ZoneId.systemDefault());
            default -> value;
        };
    }
}
