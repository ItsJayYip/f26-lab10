package edu.cmu.cs214.lab10.domain;

import java.util.Objects;

/**
 * One hold on one room for one half-open time range.
 *
 * <p>Times are raw minutes from midnight on the booking day: 540 is 09:00, 600
 * is 10:00. The range is half-open, {@code [startMinute, endMinute)}, so a
 * booking that ends at 600 and one that starts at 600 do not collide.
 *
 * <p>Bookings are immutable. A status change produces a new value; see
 * {@link #withStatus(BookingStatus)}.
 *
 * @param id           stable identifier assigned when the booking is created
 * @param roomId       the {@link Room#id()} this booking holds
 * @param startMinute  first minute held, inclusive
 * @param endMinute    first minute no longer held, exclusive
 * @param status       lifecycle state
 */
public record Booking(String id, String roomId, int startMinute, int endMinute, BookingStatus status) {

    public Booking {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(status, "status");
        if (startMinute < 0) {
            throw new IllegalArgumentException("startMinute must not be negative, got " + startMinute);
        }
        if (endMinute <= startMinute) {
            throw new IllegalArgumentException(
                    "endMinute must be after startMinute, got " + startMinute + ".." + endMinute);
        }
    }

    /**
     * Whether this booking's range shares at least one minute with the given
     * half-open range.
     *
     * <p>Ranges that merely touch at an endpoint do not overlap: {@code [540,
     * 600)} and {@code [600, 660)} are fine back to back.
     *
     * @param otherStart first minute of the other range, inclusive
     * @param otherEnd   first minute after the other range, exclusive
     * @return true when the two ranges intersect
     */
    public boolean overlaps(int otherStart, int otherEnd) {
        return startMinute < otherEnd && otherStart < endMinute;
    }

    /** Whether this booking overlaps the other one in the same room. */
    public boolean collidesWith(Booking other) {
        return roomId.equals(other.roomId()) && overlaps(other.startMinute(), other.endMinute());
    }

    /** A copy of this booking in a new state. */
    public Booking withStatus(BookingStatus newStatus) {
        return new Booking(id, roomId, startMinute, endMinute, newStatus);
    }
}
