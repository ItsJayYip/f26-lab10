package edu.cmu.cs214.lab10.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * What a booking attempt produced: a booking, or a reason there is none.
 *
 * @param outcome  how the attempt ended
 * @param booking  the booking created, absent when the attempt was rejected
 * @param reason   why the attempt was rejected, empty otherwise
 */
public record BookingResult(Outcome outcome, Optional<Booking> booking, String reason) {

    /** The three ways a booking attempt can end. */
    public enum Outcome {
        /** The caller holds the room for the slot. */
        CONFIRMED,
        /** The slot was taken, and the caller is queued behind the holder. */
        WAITLISTED,
        /** No booking was created. See {@link BookingResult#reason()}. */
        REJECTED
    }

    public BookingResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(booking, "booking");
        Objects.requireNonNull(reason, "reason");
    }

    public static BookingResult confirmed(Booking booking) {
        return new BookingResult(Outcome.CONFIRMED, Optional.of(booking), "");
    }

    public static BookingResult waitlisted(Booking booking) {
        return new BookingResult(Outcome.WAITLISTED, Optional.of(booking), "");
    }

    public static BookingResult rejected(String reason) {
        return new BookingResult(Outcome.REJECTED, Optional.empty(), reason);
    }

    public boolean isConfirmed() {
        return outcome == Outcome.CONFIRMED;
    }

    public boolean isRejected() {
        return outcome == Outcome.REJECTED;
    }
}
