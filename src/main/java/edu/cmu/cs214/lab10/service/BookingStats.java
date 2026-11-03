package edu.cmu.cs214.lab10.service;

/**
 * Rolling counters for the operations dashboard.
 *
 * <p>Thread safety: none, on purpose. The counters are plain {@code long}
 * fields incremented without any lock, so when two threads increment at the
 * same time one of the updates is lost and a reader may see a stale value.
 * Every number this class reports is approximate.
 *
 * <p>That is accepted. These counters feed a dashboard tile and a weekly
 * summary email. Nothing in the service reads them, no decision depends on
 * them, and paying for exact counting on every booking is not worth the
 * contention. If a caller ever needs exact totals, count the bookings in the
 * store instead.
 */
public final class BookingStats {

    private long confirmed;
    private long waitlisted;
    private long rejected;
    private long cancelled;

    void recordConfirmed() {
        confirmed++;
    }

    void recordWaitlisted() {
        waitlisted++;
    }

    void recordRejected() {
        rejected++;
    }

    void recordCancelled() {
        cancelled++;
    }

    /** Approximate count of confirmed bookings since startup. */
    public long confirmed() {
        return confirmed;
    }

    /** Approximate count of waitlist entries since startup. */
    public long waitlisted() {
        return waitlisted;
    }

    /** Approximate count of rejected booking attempts since startup. */
    public long rejected() {
        return rejected;
    }

    /** Approximate count of cancellations since startup. */
    public long cancelled() {
        return cancelled;
    }

    @Override
    public String toString() {
        return "confirmed=" + confirmed
                + " waitlisted=" + waitlisted
                + " rejected=" + rejected
                + " cancelled=" + cancelled
                + " (approximate)";
    }
}
