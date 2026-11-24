package com.chaintrace.verification;

import com.chaintrace.model.OwnershipRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnershipChainBuilderTest {

    private static OwnershipRecord owner(String name, String date) {
        return new OwnershipRecord(name, LocalDate.parse(date), "Resale", "Pune", Map.of());
    }

    @Test
    void sortsOwnersAndComputesPeriods() {
        // given out of order on purpose
        List<OwnershipRecord> rows = List.of(
                owner("Rohan", "2025-03-20"),
                owner("Arjun", "2023-02-14"),
                owner("Priya", "2024-01-05"));
        LocalDate today = LocalDate.parse("2026-11-21");

        List<OwnerEntry> chain = OwnershipChainBuilder.build(rows, today);

        assertEquals(3, chain.size());
        assertEquals("Arjun", chain.get(0).ownerName());
        assertEquals(1, chain.get(0).sequence());
        assertEquals(LocalDate.parse("2024-01-05"), chain.get(0).ownedUntil());
        assertEquals(325L, chain.get(0).ownershipDays()); // 2023-02-14 -> 2024-01-05
        assertFalse(chain.get(0).current());

        OwnerEntry last = chain.get(2);
        assertEquals("Rohan", last.ownerName());
        assertTrue(last.current());
        assertNull(last.ownedUntil());
        assertEquals(611L, last.ownershipDays()); // 2025-03-20 -> 2026-11-21
    }

    @Test
    void emptyHistoryGivesEmptyChain() {
        assertTrue(OwnershipChainBuilder.build(List.of(), LocalDate.now()).isEmpty());
    }
}
