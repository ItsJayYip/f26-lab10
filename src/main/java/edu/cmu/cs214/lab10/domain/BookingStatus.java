package edu.cmu.cs214.lab10.domain;

/** The lifecycle state of a {@link Booking}. */
public enum BookingStatus {

    /** The holder has the room for the slot. Only CONFIRMED bookings block others. */
    CONFIRMED,

    /** Queued behind a confirmed booking for the same room and an overlapping slot. */
    WAITLISTED,

    /** Released. A cancelled booking blocks nothing and is kept only for the audit trail. */
    CANCELLED
}
