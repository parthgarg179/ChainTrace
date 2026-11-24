package com.chaintrace.verification;

import com.chaintrace.model.OwnershipRecord;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns raw ownership rows into an ordered chain: sorts by purchase date, works out
 * when each owner's period ended (the next owner's purchase date) and how long it lasted.
 *
 * Pure logic with no Spring or database dependencies, so it is easy to unit-test.
 */
public final class OwnershipChainBuilder {

    /** Oldest purchase first; rows without a date go last. */
    private static final Comparator<OwnershipRecord> BY_PURCHASE_DATE =
            Comparator.comparing(OwnershipRecord::purchaseDate, Comparator.nullsLast(Comparator.naturalOrder()));

    private OwnershipChainBuilder() {
    }

    public static List<OwnerEntry> build(List<OwnershipRecord> records, LocalDate today) {
        List<OwnershipRecord> sorted = records.stream().sorted(BY_PURCHASE_DATE).toList();

        List<OwnerEntry> chain = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            OwnershipRecord record = sorted.get(i);
            boolean current = (i == sorted.size() - 1);
            LocalDate ownedUntil = current ? null : sorted.get(i + 1).purchaseDate();
            LocalDate periodEnd = current ? today : ownedUntil;

            Long days = (record.purchaseDate() != null && periodEnd != null)
                    ? Math.max(0, ChronoUnit.DAYS.between(record.purchaseDate(), periodEnd))
                    : null;

            chain.add(new OwnerEntry(i + 1, record.ownerName(), record.purchaseDate(), ownedUntil, days,
                    current, record.channel(), record.location(), record.details()));
        }
        return List.copyOf(chain);
    }
}
