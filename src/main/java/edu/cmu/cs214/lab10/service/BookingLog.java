package edu.cmu.cs214.lab10.service;

import edu.cmu.cs214.lab10.domain.Booking;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * The service's event log.
 *
 * <p>One line per event on standard output, in a fixed shape that the log
 * collector parses:
 *
 * <pre>
 * &lt;timestamp&gt; &lt;level&gt; &lt;event&gt; &lt;key=value ...&gt;
 * </pre>
 *
 * <p>Timestamps are UTC with millisecond precision. Keys are stable; new events
 * add keys at the end rather than reordering existing ones.
 */
public final class BookingLog {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private BookingLog() {
    }

    public static void confirmed(Booking booking) {
        write("INFO", "booking.confirmed", fields(booking));
    }

    public static void waitlisted(Booking booking) {
        write("INFO", "booking.waitlisted", fields(booking));
    }

    public static void rejected(String roomId, int startMinute, int endMinute, String reason) {
        write("INFO", "booking.rejected",
                "room=" + roomId + " start=" + startMinute + " end=" + endMinute + " reason=" + reason);
    }

    public static void cancelled(String bookingId) {
        write("INFO", "booking.cancelled", "booking=" + bookingId);
    }

    public static void promoted(Booking booking) {
        write("INFO", "booking.promoted", fields(booking));
    }

    private static String fields(Booking booking) {
        return "booking=" + booking.id()
                + " room=" + booking.roomId()
                + " start=" + booking.startMinute()
                + " end=" + booking.endMinute();
    }

    private static void write(String level, String event, String fields) {
        System.out.println(TIMESTAMP.format(Instant.now()) + " " + level + " " + event + " " + fields);
    }
}
