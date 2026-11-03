package edu.cmu.cs214.lab10.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The overlap predicate on half-open minute ranges. */
class BookingTest {

    private static Booking held(int startMinute, int endMinute) {
        return new Booking("bk-0001", "WEH-5409", startMinute, endMinute, BookingStatus.CONFIRMED);
    }

    @Test
    void partlyOverlappingRangesOverlap() {
        assertTrue(held(540, 600).overlaps(570, 630), "later range starts inside this one");
        assertTrue(held(540, 600).overlaps(510, 570), "earlier range ends inside this one");
    }

    @Test
    void containedRangesOverlap() {
        assertTrue(held(540, 660).overlaps(570, 600), "shorter range sits inside this one");
        assertTrue(held(570, 600).overlaps(540, 660), "this one sits inside a longer range");
    }

    @Test
    void adjacentRangesDoNotOverlap() {
        assertFalse(held(540, 600).overlaps(600, 660), "back to back, no shared minute");
        assertFalse(held(600, 660).overlaps(540, 600), "the same pair, the other way round");
    }

    @Test
    void disjointRangesDoNotOverlap() {
        assertFalse(held(540, 600).overlaps(720, 780));
        assertFalse(held(720, 780).overlaps(540, 600));
    }
}
